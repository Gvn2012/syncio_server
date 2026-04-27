package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final IMessagingService messagingService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("Received message from user {}: {}", userId, request.getContent());
        request.setSenderId(userId);
        messagingService.processMessage(request);
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
}
