package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.MessageResponse;
import io.github.gvn2012.messaging_service.models.Conversation;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messaging")
@RequiredArgsConstructor
public class MessagingController {

    private final IMessagingService messagingService;

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(messagingService.getConversations(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessageHistory(
            @PathVariable String conversationId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(messagingService.getMessageHistory(conversationId, userId, page, size));
    }
}
