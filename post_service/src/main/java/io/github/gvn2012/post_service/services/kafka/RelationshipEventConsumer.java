package io.github.gvn2012.post_service.services.kafka;

import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipEventConsumer {

    private final UserAffinityRepository affinityRepository;

    private static final double FOLLOW_BOOST = 1.0;
    private static final double FRIENDSHIP_BOOST = 2.0;

    @KafkaListener(topics = "relationship-events-v2", groupId = "post-service-relationship-affinity")
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userFriends", key = "#event.sourceUserId"),
            @CacheEvict(value = "userFriends", key = "#event.targetUserId"),
            @CacheEvict(value = "userBlocks", key = "#event.sourceUserId"),
            @CacheEvict(value = "userBlockedBy", key = "#event.targetUserId")
    })
    public void handleRelationshipChange(RelationshipChangedEvent event) {
        if (event == null || event.getSourceUserId() == null || event.getTargetUserId() == null)
            return;

        log.info("Handling relationship change for affinity: {} between {} and {}",
                event.getChangeType(), event.getSourceUserId(), event.getTargetUserId());

        UUID actorId = event.getSourceUserId();
        UUID authorId = event.getTargetUserId();

        updateAffinity(actorId, authorId, event.getChangeType());
    }

    private void updateAffinity(UUID actorId, UUID authorId, RelationshipChangedEvent.ChangeType type) {
        Optional<UserAffinity> existing = affinityRepository.findByUserIdAndAuthorId(actorId, authorId);
        UserAffinity aff = existing.orElseGet(() -> {
            UserAffinity newAff = new UserAffinity(null, actorId, authorId, 0.0, LocalDateTime.now(),
                    null);
            return newAff;
        });

        switch (type) {
            case FOLLOW -> aff.setAffinityScore(aff.getAffinityScore() + FOLLOW_BOOST);
            case UNFOLLOW -> aff.setAffinityScore(Math.max(0.0, aff.getAffinityScore() - FOLLOW_BOOST));
            case FRIEND_REQUEST_ACCEPTED -> {
                aff.setAffinityScore(aff.getAffinityScore() + FRIENDSHIP_BOOST);
                updatePeerAffinity(authorId, actorId, FRIENDSHIP_BOOST);
            }
            case BLOCK -> {
                aff.setAffinityScore(0.0);
                updatePeerAffinity(authorId, actorId, -1.0);
            }
            default -> {
            }
        }

        aff.setLastUpdated(LocalDateTime.now());
        affinityRepository.save(aff);
    }

    private void updatePeerAffinity(UUID actorId, UUID authorId, double boost) {
        Optional<UserAffinity> existing = affinityRepository.findByUserIdAndAuthorId(actorId, authorId);
        UserAffinity aff = existing.orElseGet(() -> {
            return new UserAffinity(null, actorId, authorId, 0.0, LocalDateTime.now(), null);
        });

        if (boost < 0) {
            aff.setAffinityScore(0.0);
        } else {
            aff.setAffinityScore(aff.getAffinityScore() + boost);
        }

        aff.setLastUpdated(LocalDateTime.now());
        affinityRepository.save(aff);
    }
}
