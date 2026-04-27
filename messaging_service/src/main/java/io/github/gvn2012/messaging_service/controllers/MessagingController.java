package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.APIResource;
import io.github.gvn2012.messaging_service.dtos.ConversationResponse;
import io.github.gvn2012.messaging_service.dtos.MessageResponse;
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
    public ResponseEntity<APIResource<List<ConversationResponse>>> getConversations(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity
                .ok(APIResource.ok("Conversations retrieved successfully", messagingService.getConversations(userId)));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<APIResource<List<MessageResponse>>> getMessageHistory(
            @PathVariable String conversationId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime before,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity
                .ok(APIResource.ok("Message history retrieved",
                        messagingService.getMessageHistory(conversationId, userId, before, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<APIResource<Long>> getTotalUnreadCount(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity
                .ok(APIResource.ok("Total unread count retrieved", messagingService.getTotalUnreadCount(userId)));
    }

    @DeleteMapping("/messages/{messageId}/recall")
    public ResponseEntity<APIResource<Void>> recallMessage(@PathVariable String messageId,
            @RequestHeader("X-User-Id") String userId) {
        messagingService.recallMessage(messageId, userId);
        return ResponseEntity.ok(APIResource.ok("Message recalled successfully", null));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<APIResource<Void>> deleteMessage(@PathVariable String messageId,
            @RequestHeader("X-User-Id") String userId) {
        messagingService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(APIResource.ok("Message deleted successfully", null));
    }
}
