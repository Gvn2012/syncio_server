package io.github.gvn2012.user_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.OrgCreatedEvent;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgCreatedConsumer {

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(topics = "org.created", groupId = "user-service-group")
    public void consume(OrgCreatedEvent event) {
        log.info("Received org created event for orgId: {}, ownerId: {}", event.getOrgId(), event.getOwnerId());

        try {
            UUID ownerId = UUID.fromString(event.getOwnerId());
            UUID orgId = UUID.fromString(event.getOrgId());
            
            User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found for ownerId: " + ownerId + " (Race condition - retrying...)"));

            user.setOrgId(orgId);
            userRepository.save(user);
            log.info("Successfully updated user {} with orgId {}", ownerId, orgId);
        } catch (Exception e) {
            log.warn("Failed to process OrgCreatedEvent. Retrying... Reason: {}", e.getMessage());
            throw new RuntimeException("Rethrowing for Kafka Retry: " + e.getMessage(), e);
        }
    }
}
