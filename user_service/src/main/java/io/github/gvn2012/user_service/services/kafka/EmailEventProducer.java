package io.github.gvn2012.user_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "email-verification";

    public void send(EmailVerificationEvent event) {
        kafkaTemplate.send(TOPIC, event.getEmail(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent email verification event for {} to partition {} at offset {}",
                                event.getEmail(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send email verification event for {}", event.getEmail(), ex);
                    }
                });
    }
}