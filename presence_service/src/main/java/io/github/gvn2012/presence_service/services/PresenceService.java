package io.github.gvn2012.presence_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.shared.kafka_events.WsOutboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRESENCE_KEY_PREFIX = "user:status:";
    private static final long ONLINE_TIMEOUT = 5;
    private static final String WS_OUTBOUND_TOPIC = "ws.outbound";

    public void setUserOnline(String userId) {
        redisTemplate.opsForValue().set(PRESENCE_KEY_PREFIX + userId, "ONLINE", ONLINE_TIMEOUT, TimeUnit.MINUTES);
        publishPresenceEvent(userId, "ONLINE");
        log.info("User {} set to ONLINE", userId);
    }

    public void setUserOffline(String userId) {
        redisTemplate.delete(PRESENCE_KEY_PREFIX + userId);
        publishPresenceEvent(userId, "OFFLINE");
        log.info("User {} set to OFFLINE", userId);
    }

    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PRESENCE_KEY_PREFIX + userId));
    }

    public List<String> getOnlineUserIds(List<String> userIds) {
        return userIds.stream()
                .filter(this::isUserOnline)
                .toList();
    }

    private void publishPresenceEvent(String userId, String status) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of("userId", userId, "status", status));

            WsOutboundEvent event = WsOutboundEvent.builder()
                    .type("PRESENCE")
                    .destination("/topic/presence")
                    .broadcast(true)
                    .payload(payload)
                    .build();

            kafkaTemplate.send(WS_OUTBOUND_TOPIC, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            log.error("Failed to publish presence event for user {}", userId, e);
        }
    }
}
