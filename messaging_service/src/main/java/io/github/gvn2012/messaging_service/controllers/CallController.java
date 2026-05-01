package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.CallSignal;
import io.github.gvn2012.messaging_service.services.impls.CallSessionService;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final IMessagingService messagingService;
    private final CallSessionService callSessionService;

    @MessageMapping("/call.signal")
    public void routeSignal(@Payload CallSignal signal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String recipientId = signal.getRecipientId();

        if ("CALL_OFFER".equals(signal.getType())) {
            if (signal.getCallId() != null) {
                if (callSessionService.getActiveSession(signal.getCallId()).isEmpty()) {
                    String callId = callSessionService.createSession(signal.getCallId(), signal.getConversationId(),
                            userId, signal.getCallMode());
                    signal.setCallId(callId);
                    callSessionService.joinSession(callId, userId);
                }
            } else {
                String callId = callSessionService.createSession(null, signal.getConversationId(), userId,
                        signal.getCallMode());
                signal.setCallId(callId);
                callSessionService.joinSession(callId, userId);
            }
        } else if ("JOIN_CALL".equals(signal.getType()) && signal.getCallId() != null) {
            callSessionService.joinSession(signal.getCallId(), userId);
        } else if ("CALL_ENDED".equals(signal.getType()) || "CALL_REJECTED".equals(signal.getType())) {
            if (signal.getCallId() != null) {
                callSessionService.leaveSession(signal.getCallId(), userId);
                boolean wasEnded = callSessionService.endSessionIfEmpty(signal.getCallId());
                if (wasEnded || "CALL_REJECTED".equals(signal.getType())) {
                    messagingService.persistCallLog(signal, userId);
                }
            } else {
                messagingService.persistCallLog(signal, userId); // Fallback for legacy clients
            }
        }

        // 2. Routing logic
        if (recipientId != null && !recipientId.equals(userId)) {
            signal.setSenderId(userId);
            log.info("Routing WebRTC signal [{}] from {} to {}", signal.getType(), userId, recipientId);
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/call", signal);
        } else if (signal.getConversationId() != null && (recipientId == null || recipientId.isEmpty())) {
            signal.setSenderId(userId);
            log.info("Broadcasting WebRTC signal [{}] to conversation {}", signal.getType(),
                    signal.getConversationId());
            messagingService.broadcastCallSignal(signal, userId);
        } else {
            log.warn("Invalid signal routing attempt. Sender: {}, Requested Recipient: {}", userId, recipientId);
        }
    }
}
