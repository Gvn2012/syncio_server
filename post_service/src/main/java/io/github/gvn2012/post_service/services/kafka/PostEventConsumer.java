package io.github.gvn2012.post_service.services.kafka;

import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import io.github.gvn2012.shared.kafka_events.PostActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final UserAffinityRepository affinityRepository;

    private static final double REACTION_WEIGHT = 0.1;
    private static final double COMMENT_WEIGHT = 0.3;
    private static final double SHARE_WEIGHT = 0.5;

    @KafkaListener(topics = "post-events-v2", groupId = "post-service-affinity")
    @Transactional
    public void handlePostActivity(PostActivityEvent event) {
        if (event == null || event.getActorId() == null || event.getAuthorId() == null) return;
        if (event.getActorId().equals(event.getAuthorId())) return;

        double increment = switch (event.getActivityType()) {
            case REACTED -> REACTION_WEIGHT;
            case COMMENTED -> COMMENT_WEIGHT;
            case SHARED -> SHARE_WEIGHT;
            default -> 0.0;
        };

        if (increment <= 0) return;

        Optional<UserAffinity> existing = affinityRepository
                .findByUserIdAndAuthorId(event.getActorId(), event.getAuthorId());

        if (existing.isPresent()) {
            UserAffinity aff = existing.get();
            aff.setAffinityScore(aff.getAffinityScore() + increment);
            aff.setLastUpdated(LocalDateTime.now());
            affinityRepository.save(aff);
        }

        log.debug("Updated affinity: actor={} author={} increment={}", event.getActorId(), event.getAuthorId(), increment);
    }
}
