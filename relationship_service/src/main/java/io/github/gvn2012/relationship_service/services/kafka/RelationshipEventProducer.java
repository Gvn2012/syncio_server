package io.github.gvn2012.relationship_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "relationship-events-v2";

    public void publishEvent(RelationshipChangedEvent event) {
        log.info("Publishing relationship event: {} for source: {} target: {}",
                event.getChangeType(), event.getSourceUserId(), event.getTargetUserId());
        String key = java.util.Objects.requireNonNull(event.getSourceUserId(), "sourceUserId must not be null")
                .toString();
        kafkaTemplate.send(TOPIC, key, event);
    }
}
