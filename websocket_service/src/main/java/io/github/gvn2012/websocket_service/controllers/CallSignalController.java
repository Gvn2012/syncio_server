package io.github.gvn2012.websocket_service.controllers;

import io.github.gvn2012.grpc.call.SessionResponse;
import io.github.gvn2012.shared.dtos.CallSignal;
import io.github.gvn2012.websocket_service.clients.CallGrpcClient;
import io.github.gvn2012.websocket_service.clients.MessagingGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class CallSignalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallGrpcClient callGrpcClient;
    private final MessagingGrpcClient messagingGrpcClient;

    @MessageMapping("/call.signal")
    public void routeSignal(@Payload CallSignal signal, SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String recipientId = signal.getRecipientId();

        // 1. Session management
        if ("CALL_OFFER".equals(signal.getType())) {
            if (signal.getCallId() != null) {
                SessionResponse existing = callGrpcClient.getActiveSession(signal.getCallId());
                if (!existing.getFound()) {
                    SessionResponse created = callGrpcClient.createSession(
                            signal.getCallId(), signal.getConversationId(), userId, signal.getCallMode());
                    signal.setCallId(created.getCallId());
                    callGrpcClient.joinSession(created.getCallId(), userId);
                }
            } else {
                SessionResponse created = callGrpcClient.createSession(
                        null, signal.getConversationId(), userId, signal.getCallMode());
                signal.setCallId(created.getCallId());
                callGrpcClient.joinSession(created.getCallId(), userId);
            }
        } else if ("JOIN_CALL".equals(signal.getType()) && signal.getCallId() != null) {
            callGrpcClient.joinSession(signal.getCallId(), userId);
        } else if ("CALL_ENDED".equals(signal.getType()) || "CALL_REJECTED".equals(signal.getType())) {
            if (signal.getCallId() != null) {
                callGrpcClient.leaveSession(signal.getCallId(), userId);
                boolean wasEnded = callGrpcClient.endSessionIfEmpty(signal.getCallId());
                if (wasEnded || "CALL_REJECTED".equals(signal.getType())) {
                    messagingGrpcClient.persistCallLog(
                            signal.getType(), signal.getSenderId(), signal.getRecipientId(),
                            signal.getCallMode(), signal.getConversationId(), signal.getCallId(),
                            signal.getCallStartTime(), userId);
                }
            } else {
                messagingGrpcClient.persistCallLog(
                        signal.getType(), signal.getSenderId(), signal.getRecipientId(),
                        signal.getCallMode(), signal.getConversationId(), signal.getCallId(),
                        signal.getCallStartTime(), userId);
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
            List<String> participants = messagingGrpcClient
                    .getConversationParticipants(signal.getConversationId());
            for (String participantId : participants) {
                if (!participantId.equals(userId)) {
                    messagingTemplate.convertAndSendToUser(participantId, "/queue/call", signal);
                }
            }
        } else {
            log.warn("Invalid signal routing attempt. Sender: {}, Requested Recipient: {}", userId, recipientId);
        }
    }
}
