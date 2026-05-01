package io.github.gvn2012.websocket_service.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.shared.kafka_events.WsOutboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaOutboundListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ws.outbound", groupId = "websocket-service-group")
    public void handleOutboundEvent(String raw) {
        try {
            WsOutboundEvent event = objectMapper.readValue(raw, WsOutboundEvent.class);

            if (event.isBroadcast()) {
                messagingTemplate.convertAndSend(event.getDestination(), event.getPayload());
                log.debug("Broadcast to {}: {}", event.getDestination(), event.getType());
            } else if (event.getRecipientIds() != null) {
                for (String userId : event.getRecipientIds()) {
                    messagingTemplate.convertAndSendToUser(userId, event.getDestination(), event.getPayload());
                }
                log.debug("Sent {} to {} users on {}", event.getType(),
                        event.getRecipientIds().size(), event.getDestination());
            }
        } catch (Exception e) {
            log.error("Failed to process ws.outbound event", e);
        }
    }
}
