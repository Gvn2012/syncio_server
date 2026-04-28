package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.dtos.ConversationRequest;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;

//test
@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final IMessagingService messagingService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received message from user {}: {}", userId, request.getContent());
        request.setSenderId(userId);
        messagingService.processMessage(request);
    }

    @MessageMapping("/chat.edit")
    public void editMessage(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("User {} editing message {}", userId, request.getId());
        messagingService.editMessage(request.getId(), request.getContent(), userId);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        messagingService.deleteMessage(messageId, userId);
    }

    @MessageMapping("/conversation.delete")
    public void deleteConversation(@Payload String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        messagingService.deleteConversation(conversationId, userId);
    }

    @MessageMapping("/conversation.create")
    public void createConversation(@Payload ConversationRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (!request.getParticipantIds().contains(userId)) {
            request.getParticipantIds().add(userId);
        }
        messagingService.createConversation(request.getParticipantIds(), request.getName(), request.getType());
    }

    @MessageMapping("/chat.ack")
    public void acknowledgeMessage(@Payload String messageId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received ACK from user {} for message {}", userId, messageId);
        messagingService.markAsDelivered(messageId, userId);
    }

    @MessageMapping("/chat.read")
    public void readMessage(@Payload String conversationId, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("User {} read conversation {}", userId, conversationId);
        messagingService.markAsSeen(conversationId, userId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = payload.get("conversationId");
        String recipientId = payload.get("recipientId");
        boolean isTyping = Boolean.parseBoolean(payload.get("isTyping"));

        messagingTemplate.convertAndSendToUser(recipientId, "/queue/typing",
                Map.of("conversationId", conversationId, "userId", userId, "isTyping", isTyping));
    }
}
