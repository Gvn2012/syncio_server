package io.github.gvn2012.user_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "email-verification";

    public void send(EmailVerificationEvent event) {
        kafkaTemplate.send(TOPIC, event.getEmail(), event);
    }
}