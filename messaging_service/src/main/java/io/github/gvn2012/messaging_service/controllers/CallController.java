package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.CallSignal;
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
    }
}
