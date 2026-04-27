package io.github.gvn2012.messaging_service.services.impls;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.models.Conversation;
import io.github.gvn2012.messaging_service.models.Message;
import io.github.gvn2012.messaging_service.models.enums.ConversationType;
import io.github.gvn2012.messaging_service.models.enums.MessageStatusType;
import io.github.gvn2012.messaging_service.repositories.ConversationRepository;
import io.github.gvn2012.messaging_service.repositories.MessageRepository;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingServiceImpl implements IMessagingService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void createConversation(List<String> participantIds, String name, String type) {
        ConversationType convType = ConversationType.valueOf(type);

        if (convType == ConversationType.DIRECT && participantIds.size() == 2) {
            Optional<Conversation> existing = conversationRepository.findDirectConversation(participantIds, 2);
            if (existing.isPresent()) {
                return;
            }
        }

        Conversation conversation = Conversation.builder()
                .participants(participantIds)
                .name(name)
                .type(convType)
                .deletedAtPerUser(new HashMap<>())
                .build();

        Conversation saved = conversationRepository.save(conversation);

        for (String participantId : participantIds) {
            messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                    Map.of("type", "CONVERSATION_CREATED", "conversation", saved));
        }
    }

    @Override
    public void processMessage(MessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getDeletedAtPerUser() != null
                && conversation.getDeletedAtPerUser().containsKey(request.getSenderId())) {
            conversation.getDeletedAtPerUser().remove(request.getSenderId());
            conversationRepository.save(conversation);

            messagingTemplate.convertAndSendToUser(request.getSenderId(), "/queue/updates",
                    Map.of("type", "CONVERSATION_RESTORED", "conversation", conversation));
        }

        Message message = Message.builder()
                .conversationId(request.getConversationId())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .status(conversation.getParticipants().stream()
                        .filter(pid -> !pid.equals(request.getSenderId()))
                        .collect(Collectors.toMap(pid -> pid, pid -> Message.StatusInfo.builder()
                                .status(MessageStatusType.SENT)
                                .updateTime(LocalDateTime.now())
                                .build())))
                .isEdited(false)
                .isDeleted(false)
                .build();

        saveMessageAndNotify(message, conversation);
    }

    @Override
    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findActiveConversationsForUser(userId);
    }

    @Override
    public List<MessageResponse> getMessageHistory(String conversationId, String userId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipants().contains(userId)) {
            throw new RuntimeException("User not authorized to view this conversation");
        }

        LocalDateTime deletedAt = null;
        if (conversation.getDeletedAtPerUser() != null) {
            deletedAt = conversation.getDeletedAtPerUser().get(userId);
        }

        Pageable pageable = PageRequest.of(page, size);

        if (deletedAt != null) {
            return messageRepository
                    .findByConversationIdAndTimestampAfterOrderByTimestampDesc(conversationId, deletedAt, pageable)
                    .getContent().stream()
                    .filter(msg -> msg.getDeletedAtPerUser() == null || !msg.getDeletedAtPerUser().containsKey(userId))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            return messageRepository.findByConversationIdOrderByTimestampDesc(conversationId, pageable)
                    .getContent().stream()
                    .filter(msg -> msg.getDeletedAtPerUser() == null || !msg.getDeletedAtPerUser().containsKey(userId))
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Async
    public void editMessage(String messageId, String newContent, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getSenderId().equals(userId) && !message.isDeleted()) {
                message.setContent(newContent);
                message.setEdited(true);
                message.setUpdatedAt(LocalDateTime.now());
                messageRepository.save(message);

                notifyParticipantsOfUpdate(message, "MESSAGE_EDITED");
            }
        });
    }

    @Override
    @Async
    public void deleteMessage(String messageId, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getSenderId().equals(userId)) {
                // Recall for everyone
                message.setDeleted(true);
                message.setContent("This message was recalled");
                message.setUpdatedAt(LocalDateTime.now());
                messageRepository.save(message);

                notifyParticipantsOfUpdate(message, "MESSAGE_RECALLED");
            } else {
                // Delete for me only
                if (message.getDeletedAtPerUser() == null) {
                    message.setDeletedAtPerUser(new HashMap<>());
                }
                message.getDeletedAtPerUser().put(userId, LocalDateTime.now());
                messageRepository.save(message);

                // Notify only the caller
                messagingTemplate.convertAndSendToUser(userId, "/queue/updates",
                        Map.of("type", "MESSAGE_DELETED_LOCAL", "messageId", messageId));
            }
        });
    }

    private void notifyParticipantsOfUpdate(Message message, String type) {
        conversationRepository.findById(message.getConversationId()).ifPresent(conversation -> {
            MessageResponse response = mapToResponse(message);
            Map<String, Object> payload = Map.of("type", type, "message", response);

            for (String participantId : conversation.getParticipants()) {
                messagingTemplate.convertAndSendToUser(participantId, "/queue/updates", payload);
            }
        });
    }

    @Override
    @Async
    public void deleteConversation(String conversationId, String userId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (conversation.getDeletedAtPerUser() == null) {
                conversation.setDeletedAtPerUser(new HashMap<>());
            }
            conversation.getDeletedAtPerUser().put(userId, LocalDateTime.now());
            conversationRepository.save(conversation);

            messagingTemplate.convertAndSendToUser(userId, "/queue/updates",
                    Map.of("type", "CONVERSATION_DELETED", "conversationId", conversationId));
        });
    }

    @Async
    protected void saveMessageAndNotify(Message message, Conversation conversation) {
        Message savedMessage = messageRepository.save(message);

        if (conversation.getDeletedAtPerUser() != null && !conversation.getDeletedAtPerUser().isEmpty()) {
            for (String participantId : conversation.getParticipants()) {
                if (conversation.getDeletedAtPerUser().containsKey(participantId)) {
                    conversation.getDeletedAtPerUser().remove(participantId);

                    messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                            Map.of("type", "CONVERSATION_RESTORED", "conversation", conversation));
                }
            }
        }

        conversation.setLastMessage(savedMessage);
        conversationRepository.save(conversation);

        MessageResponse response = mapToResponse(savedMessage);

        for (String participantId : conversation.getParticipants()) {
            if (!participantId.equals(savedMessage.getSenderId())) {
                messagingTemplate.convertAndSendToUser(participantId, "/queue/messages", response);
            }
        }

        messagingTemplate.convertAndSendToUser(savedMessage.getSenderId(), "/queue/messages", response);
    }

    @Override
    @Async
    public void markAsDelivered(String messageId, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getStatus().containsKey(userId)) {
                message.getStatus().get(userId).setStatus(MessageStatusType.DELIVERED);
                message.getStatus().get(userId).setUpdateTime(LocalDateTime.now());
                messageRepository.save(message);

                messagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/status",
                        Map.of("messageId", messageId, "userId", userId, "status", MessageStatusType.DELIVERED));
            }
        });
    }

    @Override
    @Async
    public void markAsSeen(String conversationId, String userId) {

        log.info("Marking messages as seen for conversation {} and user {}", conversationId, userId);

    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();
    }
}
