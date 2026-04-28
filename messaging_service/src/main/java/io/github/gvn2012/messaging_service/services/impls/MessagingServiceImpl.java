package io.github.gvn2012.messaging_service.services.impls;

import io.github.gvn2012.messaging_service.dtos.ConversationResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingServiceImpl implements IMessagingService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

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
    @Transactional
    public void processMessage(MessageRequest request) {
        String conversationId = request.getConversationId();
        Conversation conversation;

        if (conversationId.startsWith("direct_")) {
            Optional<Conversation> opt = conversationRepository.findById(conversationId);
            if (opt.isPresent()) {
                conversation = opt.get();
            } else {
                String[] ids = conversationId.replace("direct_", "").split("_");
                List<String> participantIds = List.of(ids);

                conversation = Conversation.builder()
                        .id(conversationId)
                        .participants(participantIds)
                        .type(ConversationType.DIRECT)
                        .deletedAtPerUser(new HashMap<>())
                        .build();
                conversation = conversationRepository.save(conversation);

                for (String participantId : participantIds) {
                    messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                            Map.of("type", "CONVERSATION_CREATED", "conversation",
                                    mapToConversationResponse(conversation, participantId)));
                }
            }
        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }

        if (conversation.getDeletedAtPerUser() != null
                && conversation.getDeletedAtPerUser().containsKey(request.getSenderId())) {
            conversation.getDeletedAtPerUser().remove(request.getSenderId());
            conversationRepository.save(conversation);

            messagingTemplate.convertAndSendToUser(request.getSenderId(), "/queue/updates",
                    Map.of("type", "CONVERSATION_RESTORED", "conversation",
                            mapToConversationResponse(conversation, request.getSenderId())));
        }

        Optional<Message> existingMessage = request.getId() != null ? messageRepository.findById(request.getId()) : Optional.empty();
        LocalDateTime timestamp = existingMessage.map(Message::getTimestamp).orElse(getCurrentTime());

        Message message = Message.builder()
                .id(request.getId() != null ? request.getId() : UUID.randomUUID().toString())
                .conversationId(request.getConversationId())
                .senderId(request.getSenderId())
                .content(request.getContent())
                .timestamp(timestamp)
                .updatedAt(getCurrentTime())
                .type(request.getType() != null ? request.getType() : io.github.gvn2012.messaging_service.models.enums.MessageType.TEXT)
                .mediaId(request.getMediaId())
                .mediaUrl(request.getMediaUrl())
                .mediaSize(request.getMediaSize())
                .mediaContentType(request.getMediaContentType())
                .status(conversation.getParticipants().stream()
                        .filter(pid -> !pid.equals(request.getSenderId()))
                        .collect(Collectors.toMap(pid -> pid, pid -> Message.StatusInfo.builder()
                                .status(MessageStatusType.SENT)
                                .updateTime(getCurrentTime())
                                .build())))
                .isEdited(false)
                .isRecalled(false)
                .build();

        saveMessageAndNotify(message, conversation);
    }

    @Override
    public List<ConversationResponse> getConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findActiveConversationsForUser(userId);
        if (conversations == null) {
            return new ArrayList<>();
        }

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, userId))
                .collect(Collectors.toList());
    }

    private ConversationResponse mapToConversationResponse(Conversation conv, String userId) {
        MessageResponse lastMessageDto = null;
        if (conv.getLastMessage() != null) {
            lastMessageDto = MessageResponse.builder()
                    .id(conv.getLastMessage().getId())
                    .conversationId(conv.getLastMessage().getConversationId())
                    .senderId(conv.getLastMessage().getSenderId())
                    .content(conv.getLastMessage().isRecalled() ? "This message was recalled"
                            : conv.getLastMessage().getContent())
                    .timestamp(conv.getLastMessage().getTimestamp())
                    .type(conv.getLastMessage().getType())
                    .mediaId(conv.getLastMessage().getMediaId())
                    .mediaUrl(conv.getLastMessage().getMediaUrl())
                    .mediaSize(conv.getLastMessage().getMediaSize())
                    .mediaContentType(conv.getLastMessage().getMediaContentType())
                    .isEdited(conv.getLastMessage().isEdited())
                    .isRecalled(conv.getLastMessage().isRecalled())
                    .build();
        }

        return ConversationResponse.builder()
                .id(conv.getId())
                .name(conv.getName())
                .participants(conv.getParticipants())
                .type(conv.getType())
                .lastMessage(lastMessageDto)
                .unreadCount((int) messageRepository.countUnreadMessages(conv.getId(), userId))
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }

    @Override
    public List<MessageResponse> getMessageHistory(String conversationId, String userId, LocalDateTime before,
            int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipants().contains(userId)) {
            throw new RuntimeException("User not authorized to view this conversation");
        }

        Pageable pageable = PageRequest.of(0, size);
        Page<Message> messagePage;

        if (before == null) {
            messagePage = messageRepository.findByConversationIdOrderByTimestampDesc(conversationId, pageable);
        } else {
            messagePage = messageRepository.findByConversationIdAndTimestampBeforeOrderByTimestampDesc(conversationId,
                    before, pageable);
        }

        // Filter out messages that have been soft-deleted for this user (via
        // message-level deletedAtPerUser)
        return messagePage.getContent().stream()
                .filter(msg -> msg.getDeletedAtPerUser() == null || !msg.getDeletedAtPerUser().containsKey(userId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void editMessage(String messageId, String newContent, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getSenderId().equals(userId) && !message.isRecalled()) {
                message.setContent(newContent);
                message.setEdited(true);
                message.setUpdatedAt(getCurrentTime());
                messageRepository.save(message);

                notifyParticipantsOfUpdate(message, "MESSAGE_EDITED");
            }
        });
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            // Unidirectional deletion: Always just mark as deleted for this user
            if (message.getDeletedAtPerUser() == null) {
                message.setDeletedAtPerUser(new HashMap<>());
            }
            message.getDeletedAtPerUser().put(userId, getCurrentTime());
            messageRepository.save(message);

            messagingTemplate.convertAndSendToUser(userId, "/queue/updates",
                    Map.of("type", "MESSAGE_DELETED_LOCAL", "messageId", messageId));
        });
    }

    @Override
    @Transactional
    public void recallMessage(String messageId, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (!message.getSenderId().equals(userId)) {
                throw new RuntimeException("Only the sender can recall a message");
            }
            if (message.isRecalled()) {
                return;
            }

            if (message.getTimestamp().plusHours(6).isBefore(getCurrentTime())) {
                throw new RuntimeException("Messages can only be recalled within 6 hours");
            }

            message.setRecalled(true);
            message.setContent("");
            message.setUpdatedAt(getCurrentTime());
            messageRepository.save(message);

            notifyParticipantsOfUpdate(message, "MESSAGE_RECALLED");
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
    @Transactional
    public void deleteConversation(String conversationId, String userId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            if (conversation.getDeletedAtPerUser() == null) {
                conversation.setDeletedAtPerUser(new HashMap<>());
            }
            LocalDateTime now = getCurrentTime();
            conversation.getDeletedAtPerUser().put(userId, now);
            conversationRepository.save(conversation);

            List<Message> allMessages = messageRepository.findByConversationIdOrderByTimestampDesc(
                    conversationId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            for (Message msg : allMessages) {
                if (msg.getDeletedAtPerUser() == null) {
                    msg.setDeletedAtPerUser(new HashMap<>());
                }
                if (!msg.getDeletedAtPerUser().containsKey(userId)) {
                    msg.getDeletedAtPerUser().put(userId, now);
                }
            }
            messageRepository.saveAll(allMessages);

            messagingTemplate.convertAndSendToUser(userId, "/queue/updates",
                    Map.of("type", "CONVERSATION_DELETED", "conversationId", conversationId));
        });
    }

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
    @Transactional
    public void markAsDelivered(String messageId, String userId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            if (message.getStatus().containsKey(userId)) {
                message.getStatus().get(userId).setStatus(MessageStatusType.DELIVERED);
                message.getStatus().get(userId).setUpdateTime(getCurrentTime());
                messageRepository.save(message);

                messagingTemplate.convertAndSendToUser(message.getSenderId(), "/queue/status",
                        Map.of(
                                "conversationId", message.getConversationId(),
                                "messageId", messageId,
                                "userId", userId,
                                "status", MessageStatusType.DELIVERED));
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsDelivered(String userId) {
        log.info("Marking all undelivered messages as delivered for user {}", userId);

        Query query = new Query();
        query.addCriteria(Criteria.where("senderId").ne(userId));
        query.addCriteria(Criteria.where("status." + userId + ".status").is("SENT"));

        List<Message> undeliveredMessages = mongoTemplate.find(query, Message.class);

        if (undeliveredMessages.isEmpty())
            return;

        for (Message msg : undeliveredMessages) {
            if (msg.getStatus().containsKey(userId)) {
                msg.getStatus().get(userId).setStatus(MessageStatusType.DELIVERED);
                msg.getStatus().get(userId).setUpdateTime(getCurrentTime());
            }
        }

        messageRepository.saveAll(undeliveredMessages);

        Map<String, Map<String, List<String>>> grouped = undeliveredMessages.stream()
                .collect(Collectors.groupingBy(Message::getSenderId,
                        Collectors.groupingBy(Message::getConversationId,
                                Collectors.mapping(Message::getId, Collectors.toList()))));

        grouped.forEach((senderId, convMap) -> {
            convMap.forEach((conversationId, messageIds) -> {
                messagingTemplate.convertAndSendToUser(senderId, "/queue/status",
                        Map.of(
                                "conversationId", conversationId,
                                "messageIds", messageIds,
                                "userId", userId,
                                "status", MessageStatusType.DELIVERED));
            });
        });
    }

    @Override
    @Transactional
    public void markAsSeen(String conversationId, String userId) {
        log.info("Marking messages as seen for conversation {} and user {}", conversationId, userId);

        Query query = new Query();
        query.addCriteria(Criteria.where("conversationId").is(conversationId));
        query.addCriteria(Criteria.where("senderId").ne(userId));
        query.addCriteria(Criteria.where("status." + userId + ".status").ne("SEEN"));

        List<Message> unreadMessages = mongoTemplate.find(query, Message.class);

        if (unreadMessages.isEmpty())
            return;

        for (Message msg : unreadMessages) {
            if (msg.getStatus().containsKey(userId)) {
                msg.getStatus().get(userId).setStatus(MessageStatusType.SEEN);
                msg.getStatus().get(userId).setUpdateTime(getCurrentTime());
            }
        }

        messageRepository.saveAll(unreadMessages);

        Map<String, List<String>> messagesBySender = unreadMessages.stream()
                .collect(Collectors.groupingBy(Message::getSenderId,
                        Collectors.mapping(Message::getId, Collectors.toList())));

        messagesBySender.forEach((senderId, messageIds) -> {
            messagingTemplate.convertAndSendToUser(senderId, "/queue/status",
                    Map.of(
                            "conversationId", conversationId,
                            "messageIds", messageIds,
                            "userId", userId,
                            "status", MessageStatusType.SEEN));
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public long getTotalUnreadCount(String userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("senderId").ne(userId)
                        .and("status." + userId + ".status").ne(MessageStatusType.SEEN)
                        .and("deletedAtPerUser." + userId).exists(false)),

                Aggregation.lookup("conversations", "conversationId", "_id", "conversation"),
                Aggregation.unwind("conversation"),

                Aggregation.match(Criteria.where("conversation.deletedAtPerUser." + userId).exists(false)),

                Aggregation.count().as("total"));

        AggregationResults<Map<String, Object>> results = mongoTemplate.aggregate(aggregation, "messages",
                (Class<Map<String, Object>>) (Class<?>) Map.class);
        Map<String, Object> result = results.getUniqueMappedResult();
        return result != null ? ((Number) result.get("total")).longValue() : 0L;
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .type(message.getType())
                .mediaId(message.getMediaId())
                .mediaUrl(message.getMediaUrl())
                .mediaSize(message.getMediaSize())
                .mediaContentType(message.getMediaContentType())
                .timestamp(message.getTimestamp())
                .isEdited(message.isEdited())
                .isRecalled(message.isRecalled())
                .status(message.getStatus())
                .build();
    }
}
