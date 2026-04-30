package io.github.gvn2012.messaging_service.services.impls;

import io.github.gvn2012.messaging_service.dtos.ConversationResponse;
import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.dtos.CallSignal;
import io.github.gvn2012.messaging_service.models.Conversation;
import io.github.gvn2012.messaging_service.models.Message;
import io.github.gvn2012.messaging_service.models.MediaItem;
import io.github.gvn2012.messaging_service.models.enums.ConversationType;
import io.github.gvn2012.messaging_service.models.enums.MessageStatusType;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import io.github.gvn2012.messaging_service.repositories.ConversationRepository;
import io.github.gvn2012.messaging_service.repositories.MessageRepository;
import io.github.gvn2012.messaging_service.repositories.MediaItemRepository;
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
import net.devh.boot.grpc.client.inject.GrpcClient;
import io.github.gvn2012.grpc.user.UserServiceGrpc;
import io.github.gvn2012.grpc.user.UserBatchRequest;
import io.github.gvn2012.grpc.user.UserSummaryBatchResponse;
import io.github.gvn2012.grpc.user.UserSummary;
import io.github.gvn2012.messaging_service.dtos.GroupCreateRequest;
import io.github.gvn2012.messaging_service.dtos.GroupMemberRequest;
import io.github.gvn2012.messaging_service.dtos.GroupSummaryResponse;
import io.github.gvn2012.messaging_service.dtos.GroupUpdateRequest;
import io.github.gvn2012.messaging_service.dtos.ParticipantPreview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
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
    private final MediaItemRepository mediaItemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoTemplate mongoTemplate;

    @GrpcClient("user_service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

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
    public void createGroupConversation(GroupCreateRequest request, String creatorId) {
        List<String> participants = new ArrayList<>(request.getParticipantIds());
        if (!participants.contains(creatorId)) {
            participants.add(creatorId);
        }

        if (participants.size() < 2 || participants.size() > 50) {
            throw new IllegalArgumentException("Group must have between 2 and 50 participants");
        }

        String groupName = request.getName();
        Map<String, UserSummary> userSummaries = new HashMap<>();

        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(participants).build());
            userSummaries.putAll(response.getSummariesMap());
        } catch (Exception e) {
            log.error("Failed to fetch user summaries for group name generation", e);
        }

        if (groupName == null || groupName.trim().isEmpty()) {
            List<String> names = participants.stream()
                    .filter(id -> !id.equals(creatorId))
                    .map(id -> userSummaries.containsKey(id) ? userSummaries.get(id).getDisplayName() : "User")
                    .limit(3)
                    .collect(Collectors.toList());
            groupName = String.join(", ", names);
            if (participants.size() > 4) {
                groupName += " and " + (participants.size() - 4) + " others";
            }
        }

        Conversation conversation = Conversation.builder()
                .participants(participants)
                .name(groupName)
                .type(ConversationType.GROUP)
                .adminIds(List.of(creatorId))
                .maxSize(50)
                .deletedAtPerUser(new HashMap<>())
                .build();

        Conversation saved = conversationRepository.save(conversation);

        String creatorName = getUserName(creatorId);
        int others = participants.size() - 2;
        String initMsg = others > 0
                ? String.format("%s has added you and %d others to this conversation", creatorName, others)
                : String.format("%s has added you to this conversation", creatorName);
        sendSystemMessage(saved, initMsg);

        for (String participantId : participants) {
            ConversationResponse convResponse = mapToConversationResponse(saved, participantId, userSummaries);
            messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                    Map.of("type", "CONVERSATION_CREATED", "conversation", convResponse));
        }
    }

    @Override
    public void updateGroupConversation(GroupUpdateRequest request, String userId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP || conversation.getAdminIds() == null
                || !conversation.getAdminIds().contains(userId)) {
            throw new RuntimeException("Only admins can update group details");
        }

        String oldName = conversation.getName();
        if (request.getName() != null)
            conversation.setName(request.getName());
        if (request.getDescription() != null)
            conversation.setDescription(request.getDescription());
        if (request.getGroupAvatar() != null)
            conversation.setGroupAvatar(request.getGroupAvatar());

        Conversation saved = conversationRepository.save(conversation);

        if (request.getName() != null && !request.getName().equals(oldName)) {
            sendSystemMessage(saved, String.format("Group name changed to \"%s\"", request.getName()));
        }

        for (String participantId : saved.getParticipants()) {
            messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                    Map.of("type", "GROUP_UPDATED", "conversation", mapToConversationResponse(saved, participantId)));
        }
    }

    private String getUserName(String userId) {
        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(UserBatchRequest.newBuilder()
                    .addUserIds(userId)
                    .build());
            UserSummary summary = response.getSummariesMap().get(userId);
            if (summary != null) {
                return summary.getDisplayName();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user name for {}", userId, e);
        }
        return "A member";
    }

    @Override
    @Transactional
    public void manageGroupMembers(GroupMemberRequest request, String adminId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP || conversation.getAdminIds() == null
                || !conversation.getAdminIds().contains(adminId)) {
            throw new RuntimeException("Only admins can manage members");
        }

        List<String> participants = new ArrayList<>(conversation.getParticipants());
        List<String> admins = new ArrayList<>(conversation.getAdminIds());
        String systemMessageContent = null;

        switch (request.getAction().toUpperCase()) {
            case "ADD":
                if (participants.size() >= conversation.getMaxSize())
                    throw new RuntimeException("Group is full");
                if (!participants.contains(request.getUserId())) {
                    participants.add(request.getUserId());
                    systemMessageContent = String.format("%s has been added to the group",
                            getUserName(request.getUserId()));
                }
                break;
            case "REMOVE":
                if (request.getUserId().equals(adminId))
                    throw new RuntimeException("Cannot remove yourself");
                if (participants.contains(request.getUserId())) {
                    String userName = getUserName(request.getUserId());
                    participants.remove(request.getUserId());
                    admins.remove(request.getUserId());
                    systemMessageContent = String.format("%s has been removed from the group", userName);
                }
                break;
            case "PROMOTE":
                if (participants.contains(request.getUserId()) && !admins.contains(request.getUserId())) {
                    admins.add(request.getUserId());
                    systemMessageContent = String.format("%s has been promoted to admin",
                            getUserName(request.getUserId()));
                }
                break;
            case "DEMOTE":
                if (request.getUserId().equals(adminId))
                    throw new RuntimeException("Cannot demote yourself");
                if (admins.contains(request.getUserId())) {
                    admins.remove(request.getUserId());
                    systemMessageContent = String.format("%s has been demoted", getUserName(request.getUserId()));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + request.getAction());
        }

        conversation.setParticipants(participants);
        conversation.setAdminIds(admins);
        Conversation saved = conversationRepository.save(conversation);

        if (systemMessageContent != null) {
            sendSystemMessage(saved, systemMessageContent);
        }

        for (String participantId : saved.getParticipants()) {
            messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                    Map.of("type", "GROUP_UPDATED", "conversation", mapToConversationResponse(saved, participantId)));
        }

        if ("REMOVE".equals(request.getAction().toUpperCase())) {
            messagingTemplate.convertAndSendToUser(request.getUserId(), "/queue/updates",
                    Map.of("type", "CONVERSATION_DELETED", "conversationId", saved.getId()));
        }
    }

    @Override
    @Transactional
    public void leaveGroupConversation(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (conversation.getType() != ConversationType.GROUP) {
            throw new RuntimeException("Cannot leave a direct conversation");
        }

        List<String> participants = new ArrayList<>(conversation.getParticipants());
        List<String> admins = new ArrayList<>(conversation.getAdminIds());

        if (!participants.contains(userId)) {
            return;
        }

        String userName = getUserName(userId);
        participants.remove(userId);
        admins.remove(userId);

        if (participants.isEmpty()) {
            conversationRepository.delete(conversation);
            return;
        }

        if (admins.isEmpty() && !participants.isEmpty()) {
            admins.add(participants.get(0));
        }

        conversation.setParticipants(participants);
        conversation.setAdminIds(admins);
        Conversation saved = conversationRepository.save(conversation);

        sendSystemMessage(saved, String.format("%s has left the group chat", userName));

        messagingTemplate.convertAndSendToUser(userId, "/queue/updates",
                Map.of("type", "CONVERSATION_DELETED", "conversationId", saved.getId()));

        for (String participantId : saved.getParticipants()) {
            messagingTemplate.convertAndSendToUser(participantId, "/queue/updates",
                    Map.of("type", "GROUP_UPDATED", "conversation", mapToConversationResponse(saved, participantId)));
        }
    }

    private void sendSystemMessage(Conversation conversation, String content) {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversation.getId())
                .senderId("SYSTEM")
                .content(content)
                .timestamp(getCurrentTime())
                .updatedAt(getCurrentTime())
                .type(MessageType.SYSTEM)
                .status(conversation.getParticipants().stream()
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
    public GroupSummaryResponse getGroupSummary(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getParticipants().contains(userId)) {
            throw new RuntimeException("User not authorized");
        }

        Map<String, UserSummary> userSummaries = new HashMap<>();
        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(conversation.getParticipants()).build());
            userSummaries.putAll(response.getSummariesMap());
        } catch (Exception e) {
            log.error("Failed to fetch user summaries for group {}", conversationId, e);
        }

        List<ParticipantPreview> previews = conversation.getParticipants().stream()
                .map(id -> {
                    UserSummary summary = userSummaries.get(id);
                    return ParticipantPreview.builder()
                            .userId(id)
                            .displayName(summary != null ? summary.getDisplayName() : "Unknown")
                            .profilePictureUrl(summary != null ? summary.getAvatarUrl() : null)
                            .build();
                }).collect(Collectors.toList());

        return GroupSummaryResponse.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .groupAvatar(conversation.getGroupAvatar())
                .description(conversation.getDescription())
                .adminIds(conversation.getAdminIds())
                .maxSize(conversation.getMaxSize())
                .participantCount(conversation.getParticipants().size())
                .participantPreviews(previews)
                .type(conversation.getType())
                .lastMessage(
                        conversation.getLastMessage() != null ? mapToResponse(conversation.getLastMessage()) : null)
                .unreadCount((int) messageRepository.countUnreadMessages(conversation.getId(), userId))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    @Override
    public void broadcastTyping(String conversationId, String userId, boolean isTyping) {
        Optional<Conversation> opt = conversationRepository.findById(conversationId);
        if (opt.isPresent()) {
            Conversation conv = opt.get();
            for (String participantId : conv.getParticipants()) {
                if (!participantId.equals(userId)) {
                    messagingTemplate.convertAndSendToUser(participantId, "/queue/typing",
                            Map.of("conversationId", conversationId, "userId", userId, "isTyping", isTyping));
                }
            }
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

        Optional<Message> existingMessage = Optional.empty();
        if (request.getId() != null) {
            existingMessage = messageRepository.findById(request.getId());
        } else if (request.getBatchId() != null) {
            existingMessage = messageRepository.findByConversationIdAndBatchId(request.getConversationId(),
                    request.getBatchId());
        }

        LocalDateTime timestamp = existingMessage.map(Message::getTimestamp).orElse(getCurrentTime());

        Message message;
        if (existingMessage.isPresent()) {
            message = existingMessage.get();
            message.setUpdatedAt(getCurrentTime());

            if (request.getContent() != null && !request.getContent().isEmpty()) {
                message.setContent(request.getContent());
            }
            if (request.getType() != null) {
                message.setType(request.getType());
            }
        } else {
            message = Message.builder()
                    .id(request.getId() != null ? request.getId() : UUID.randomUUID().toString())
                    .conversationId(request.getConversationId())
                    .batchId(request.getBatchId())
                    .senderId(request.getSenderId())
                    .content(request.getContent())
                    .timestamp(timestamp)
                    .updatedAt(getCurrentTime())
                    .type(request.getType() != null ? request.getType()
                            : io.github.gvn2012.messaging_service.models.enums.MessageType.TEXT)
                    .status(conversation.getParticipants().stream()
                            .filter(pid -> !pid.equals(request.getSenderId()))
                            .collect(Collectors.toMap(pid -> pid, pid -> Message.StatusInfo.builder()
                                    .status(MessageStatusType.SENT)
                                    .updateTime(getCurrentTime())
                                    .build())))
                    .isEdited(false)
                    .isRecalled(false)
                    .build();
        }

        if (request.getMediaItems() != null && !request.getMediaItems().isEmpty()) {
            for (MediaItem newItem : request.getMediaItems()) {
                if (newItem.getBatchId() == null) {
                    newItem.setBatchId(message.getBatchId());
                }
                if (newItem.getConversationId() == null) {
                    newItem.setConversationId(message.getConversationId());
                }
                mediaItemRepository.save(newItem);
            }
        }

        saveMessageAndNotify(message, conversation);
    }

    @Override
    @Transactional
    public void persistCallLog(CallSignal signal, String userId) {
        String conversationId = signal.getConversationId();
        if (conversationId == null)
            return;

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null)
            return;

        boolean isVideo = "VIDEO".equals(signal.getCallMode());
        MessageType type = isVideo ? MessageType.CALL_VIDEO : MessageType.CALL_VOICE;

        String content;
        if ("CALL_REJECTED".equals(signal.getType())) {
            content = isVideo ? "Missed video call" : "Missed voice call";
        } else {
            int duration = 0;
            if (signal.getPayload() instanceof Map) {
                Map<?, ?> payloadMap = (Map<?, ?>) signal.getPayload();
                if (payloadMap.containsKey("duration")) {
                    Object durationObj = payloadMap.get("duration");
                    if (durationObj instanceof Number) {
                        duration = ((Number) durationObj).intValue();
                    } else if (durationObj instanceof String) {
                        try {
                            duration = Integer.parseInt((String) durationObj);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            if (duration == 0) {
                content = isVideo ? "Missed video call" : "Missed voice call";
            } else {
                int mins = duration / 60;
                int secs = duration % 60;
                String timeStr = String.format("%02d:%02d", mins, secs);
                content = (isVideo ? "Video call \u2022 " : "Voice call \u2022 ") + timeStr;
            }
        }

        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .senderId(userId)
                .content(content)
                .timestamp(getCurrentTime())
                .updatedAt(getCurrentTime())
                .type(type)
                .status(conversation.getParticipants().stream()
                        .filter(pid -> !pid.equals(userId))
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
        if (conversations == null || conversations.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> allParticipantIds = conversations.stream()
                .flatMap(conv -> conv.getParticipants().stream())
                .distinct()
                .collect(Collectors.toList());

        Map<String, UserSummary> userSummaries = new HashMap<>();
        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(allParticipantIds).build());
            userSummaries.putAll(response.getSummariesMap());
        } catch (Exception e) {
            log.error("Failed to fetch user summaries for getConversations", e);
        }

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, userId, userSummaries))
                .collect(Collectors.toList());
    }

    private ConversationResponse mapToConversationResponse(Conversation conv, String userId) {
        Map<String, UserSummary> userSummaries = new HashMap<>();
        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(conv.getParticipants()).build());
            userSummaries.putAll(response.getSummariesMap());
        } catch (Exception e) {
            log.error("Failed to fetch user summaries for mapToConversationResponse", e);
        }
        return mapToConversationResponse(conv, userId, userSummaries);
    }

    private ConversationResponse mapToConversationResponse(Conversation conv, String userId,
            Map<String, UserSummary> userSummaries) {
        MessageResponse lastMessageDto = conv.getLastMessage() != null ? mapToResponse(conv.getLastMessage()) : null;

        List<ParticipantPreview> previews = conv.getParticipants().stream()
                .map(id -> {
                    UserSummary summary = userSummaries != null ? userSummaries.get(id) : null;
                    return ParticipantPreview.builder()
                            .userId(id)
                            .displayName(summary != null ? summary.getDisplayName() : "Unknown")
                            .profilePictureUrl(summary != null ? summary.getAvatarUrl() : null)
                            .build();
                }).collect(Collectors.toList());

        return ConversationResponse.builder()
                .id(conv.getId())
                .name(conv.getName())
                .participants(conv.getParticipants())
                .type(conv.getType())
                .lastMessage(lastMessageDto)
                .unreadCount((int) messageRepository.countUnreadMessages(conv.getId(), userId))
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .groupAvatar(conv.getGroupAvatar())
                .description(conv.getDescription())
                .adminIds(conv.getAdminIds())
                .maxSize(conv.getMaxSize())
                .participantPreviews(previews)
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

                conversationRepository.findById(message.getConversationId()).ifPresent(conversation -> {
                    for (String participantId : conversation.getParticipants()) {
                        if (!participantId.equals(userId)) {
                            messagingTemplate.convertAndSendToUser(participantId, "/queue/status",
                                    Map.of(
                                            "conversationId", message.getConversationId(),
                                            "messageId", messageId,
                                            "userId", userId,
                                            "status", MessageStatusType.DELIVERED));
                        }
                    }
                });
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
                conversationRepository.findById(conversationId).ifPresent(conversation -> {
                    for (String participantId : conversation.getParticipants()) {
                        if (!participantId.equals(userId)) {
                            messagingTemplate.convertAndSendToUser(participantId, "/queue/status",
                                    Map.of(
                                            "conversationId", conversationId,
                                            "messageIds", messageIds,
                                            "userId", userId,
                                            "status", MessageStatusType.DELIVERED));
                        }
                    }
                });
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

        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            messagesBySender.forEach((senderId, messageIds) -> {
                for (String participantId : conversation.getParticipants()) {
                    if (!participantId.equals(userId)) {
                        messagingTemplate.convertAndSendToUser(participantId, "/queue/status",
                                Map.of(
                                        "conversationId", conversationId,
                                        "messageIds", messageIds,
                                        "userId", userId,
                                        "status", MessageStatusType.SEEN));
                    }
                }
            });
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

    @Override
    public void broadcastCallSignal(CallSignal signal, String userId) {
        conversationRepository.findById(signal.getConversationId()).ifPresent(conversation -> {
            for (String participantId : conversation.getParticipants()) {
                if (!participantId.equals(userId)) {
                    messagingTemplate.convertAndSendToUser(participantId, "/queue/call", signal);
                }
            }
        });
    }

    private MessageResponse mapToResponse(Message message) {
        List<MediaItem> mediaItems = message.getBatchId() != null
                ? mediaItemRepository.findByBatchId(message.getBatchId())
                : Collections.emptyList();

        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .batchId(message.getBatchId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .type(message.getType())
                .mediaItems(mediaItems)
                .isEdited(message.isEdited())
                .isRecalled(message.isRecalled())
                .status(message.getStatus())
                .build();
    }
}
