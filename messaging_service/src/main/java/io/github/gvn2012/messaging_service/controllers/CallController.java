package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.CallSignal;
import io.github.gvn2012.messaging_service.models.GroupCallRoom;
import io.github.gvn2012.messaging_service.repositories.GroupCallRoomRepository;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IMessagingService messagingService;
    private final GroupCallRoomRepository groupCallRoomRepository;

    @MessageMapping("/call.signal")
    public void routeSignal(@Payload CallSignal signal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String recipientId = signal.getRecipientId();

        if (recipientId != null && !recipientId.equals(userId)) {
            signal.setSenderId(userId);

            log.info("Routing WebRTC signal [{}] from {} to {}", signal.getType(), userId, recipientId);

            messagingTemplate.convertAndSendToUser(recipientId, "/queue/call", signal);
        } else {
            log.warn("Invalid signal routing attempt. Sender: {}, Requested Recipient: {}", userId, recipientId);
        }

        if ("CALL_ENDED".equals(signal.getType()) || "CALL_REJECTED".equals(signal.getType())) {
            messagingService.persistCallLog(signal, userId);
        }

        if (signal.getConversationId() != null && (recipientId == null || recipientId.isEmpty())) {
            messagingService.broadcastCallSignal(signal, userId);
        }
    }

    @MessageMapping("/call.group.join")
    public void joinGroupCall(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = payload.get("conversationId");
        String callMode = payload.get("callMode");

        log.info("User {} joining group call in conversation {}", userId, conversationId);

        GroupCallRoom room = groupCallRoomRepository.findById(conversationId)
                .orElseGet(() -> GroupCallRoom.builder()
                        .conversationId(conversationId)
                        .callMode(callMode)
                        .build());
        
        room.getActiveParticipantIds().add(userId);
        groupCallRoomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/call.group." + conversationId, Map.of(
                "type", "USER_JOINED",
                "userId", userId,
                "activeParticipants", room.getActiveParticipantIds()
        ));
    }

    @MessageMapping("/call.group.signal")
    public void routeGroupSignal(@Payload CallSignal signal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        signal.setSenderId(userId);
        
        String recipientId = signal.getRecipientId();
        if (recipientId != null) {
            log.debug("Routing Group WebRTC signal [{}] from {} to {}", signal.getType(), userId, recipientId);
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/call.group", signal);
        }
    }

    @MessageMapping("/call.group.leave")
    public void leaveGroupCall(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = payload.get("conversationId");

        log.info("User {} leaving group call in conversation {}", userId, conversationId);

        groupCallRoomRepository.findById(conversationId).ifPresent(room -> {
            room.getActiveParticipantIds().remove(userId);
            if (room.getActiveParticipantIds().isEmpty()) {
                groupCallRoomRepository.delete(room);
            } else {
                groupCallRoomRepository.save(room);
            }

            messagingTemplate.convertAndSend("/topic/call.group." + conversationId, Map.of(
                    "type", "USER_LEFT",
                    "userId", userId,
                    "activeParticipants", room.getActiveParticipantIds()
            ));
        });
    }
}
