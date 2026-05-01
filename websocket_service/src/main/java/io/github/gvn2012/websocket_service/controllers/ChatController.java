package io.github.gvn2012.websocket_service.controllers;

import io.github.gvn2012.grpc.messaging.MediaItemProto;
import io.github.gvn2012.grpc.messaging.ProcessMessageRequest;
import io.github.gvn2012.websocket_service.clients.MessagingGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final MessagingGrpcClient messagingGrpcClient;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received message from user {}", userId);

        ProcessMessageRequest.Builder builder = ProcessMessageRequest.newBuilder()
                .setSenderId(userId);

        if (request.get("id") != null) builder.setId((String) request.get("id"));
        if (request.get("batchId") != null) builder.setBatchId((String) request.get("batchId"));
        if (request.get("conversationId") != null)
            builder.setConversationId((String) request.get("conversationId"));
        if (request.get("content") != null) builder.setContent((String) request.get("content"));
        if (request.get("type") != null) builder.setType((String) request.get("type"));

        if (request.get("mediaItems") instanceof List<?> mediaItems) {
            for (Object item : mediaItems) {
                if (item instanceof Map<?, ?> mediaMap) {
                    MediaItemProto.Builder mediaBuilder = MediaItemProto.newBuilder();
                    if (mediaMap.get("id") != null) mediaBuilder.setId((String) mediaMap.get("id"));
                    if (mediaMap.get("batchId") != null)
                        mediaBuilder.setBatchId((String) mediaMap.get("batchId"));
                    if (mediaMap.get("conversationId") != null)
                        mediaBuilder.setConversationId((String) mediaMap.get("conversationId"));
                    if (mediaMap.get("fileName") != null)
                        mediaBuilder.setFileName((String) mediaMap.get("fileName"));
                    if (mediaMap.get("contentType") != null)
                        mediaBuilder.setContentType((String) mediaMap.get("contentType"));
                    if (mediaMap.get("mediaType") != null)
                        mediaBuilder.setMediaType((String) mediaMap.get("mediaType"));
                    if (mediaMap.get("status") != null)
                        mediaBuilder.setStatus((String) mediaMap.get("status"));
                    builder.addMediaItems(mediaBuilder.build());
                }
            }
        }

        messagingGrpcClient.processMessage(builder.build());
    }

    @MessageMapping("/chat.edit")
    public void editMessage(@Payload Map<String, String> request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("User {} editing message {}", userId, request.get("id"));
        messagingGrpcClient.editMessage(request.get("id"), request.get("content"), userId);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        messagingGrpcClient.deleteMessage(messageId, userId);
    }

    @MessageMapping("/conversation.delete")
    public void deleteConversation(@Payload String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        messagingGrpcClient.deleteConversation(conversationId, userId);
    }

    @MessageMapping("/conversation.create")
    public void createConversation(@Payload Map<String, Object> request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        @SuppressWarnings("unchecked")
        List<String> participantIds = (List<String>) request.get("participantIds");
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
        String name = (String) request.getOrDefault("name", "");
        String type = (String) request.getOrDefault("type", "DIRECT");
        messagingGrpcClient.createConversation(participantIds, name, type);
    }

    @MessageMapping("/group.create")
    public void createGroupConversation(@Payload Map<String, Object> request,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received group creation request from user {}: {}", userId, request.get("name"));
        @SuppressWarnings("unchecked")
        List<String> memberIds = (List<String>) request.get("memberIds");
        messagingGrpcClient.createGroup(
                (String) request.get("name"),
                (String) request.get("avatarUrl"),
                memberIds,
                userId);
    }

    @MessageMapping("/group.update")
    public void updateGroupConversation(@Payload Map<String, String> request,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        messagingGrpcClient.updateGroup(
                request.get("conversationId"), request.get("name"),
                request.get("avatarUrl"), userId);
    }

    @MessageMapping("/group.members")
    public void manageGroupMembers(@Payload Map<String, Object> request,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        messagingGrpcClient.manageGroupMembers(
                (String) request.get("conversationId"),
                (String) request.get("action"),
                userIds,
                userId);
    }

    @MessageMapping("/group.leave")
    public void leaveGroupConversation(@Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = payload.get("conversationId");
        messagingGrpcClient.leaveGroup(conversationId, userId);
    }

    @MessageMapping("/chat.ack")
    public void acknowledgeMessage(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received ACK from user {} for message {}", userId, messageId);
        messagingGrpcClient.markAsDelivered(messageId, userId);
    }

    @MessageMapping("/chat.read")
    public void readMessage(@Payload String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("User {} read conversation {}", userId, conversationId);
        messagingGrpcClient.markAsSeen(conversationId, userId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = payload.get("conversationId");
        String recipientId = payload.get("recipientId");
        boolean isTyping = Boolean.parseBoolean(payload.get("isTyping"));

        if (recipientId != null && !recipientId.isEmpty() && !recipientId.equals("null")
                && !recipientId.equals("undefined")) {
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/typing",
                    Map.of("conversationId", conversationId, "userId", userId, "isTyping", isTyping));
        } else {
            messagingGrpcClient.broadcastTyping(conversationId, userId, isTyping);
        }
    }
}
