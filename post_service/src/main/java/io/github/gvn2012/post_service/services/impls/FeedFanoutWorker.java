package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.repositories.FeedItemRepository;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedFanoutWorker {

    private static final int FANOUT_THRESHOLD = 50_000;

    private final FeedItemRepository feedItemRepository;
    private final UserAffinityRepository affinityRepository;
    private final RelationshipClient relationshipClient;

    public record PostCreatedEvent(Post post) {
    }

    @Async
    @TransactionalEventListener
    public void handlePostCreated(PostCreatedEvent event) {
        Post post = event.post();
        List<UUID> audience = lookupAudience(post.getAuthorId());

        if (audience.size() > FANOUT_THRESHOLD) {
            log.info("Skipping fanout for high-volume author {} ({} audience members)", post.getAuthorId(),
                    audience.size());
            return;
        }

        Map<UUID, UserAffinity> affinities = affinityRepository.findByUserIdInAndAuthorId(audience, post.getAuthorId())
                .stream()
                .collect(Collectors.toMap(UserAffinity::getUserId, a -> a));

        List<FeedItem> feedItems = audience.stream()
                .filter(recipientId -> canViewPost(post, recipientId, audience))
                .map(recipientId -> {
                    FeedItem item = new FeedItem();
                    item.setRecipientId(recipientId);
                    item.setSourcePost(post);
                    item.setOrgId(post.getOrgId());
                    item.setWeightScore(calculateInitialWeight(post, affinities.get(recipientId)));
                    item.setReasonCode("audience");
                    item.setIsRead(false);
                    item.setIsHidden(false);
                    return item;
                }).toList();

        if (!feedItems.isEmpty()) {
            feedItemRepository.saveAll(feedItems);
            log.info("Fan-out {} feed items for post {}", feedItems.size(), post.getId());
        }
    }

    private List<UUID> lookupAudience(UUID authorId) {
        try {
            return new java.util.ArrayList<>(relationshipClient.getAudience(authorId)
                    .timeout(java.time.Duration.ofMillis(5000))
                    .onErrorReturn(java.util.Set.of())
                    .block());
        } catch (Exception e) {
            log.warn("Failed to fetch audience for {}: {}", authorId, e.getMessage());
            return List.of();
        }
    }

    private boolean canViewPost(Post post, UUID recipientId, List<UUID> audience) {
        if (post.getAuthorId().equals(recipientId))
            return true;

        switch (post.getVisibility()) {
            case PUBLIC, COMPANY -> {
                return true;
            }
            case FOLLOWER -> {

                return true;
            }
            case PRIVATE -> {
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    private double calculateInitialWeight(Post post, UserAffinity affinity) {
        double baseWeight = 1.0;

        double categoryBoost = switch (post.getPostCategory()) {
            case ANNOUNCEMENT -> 2.0;
            case TASK, EVENT -> 1.5;
            case POLL -> 1.2;
            default -> 1.0;
        };

        baseWeight *= categoryBoost;

        if (affinity != null && affinity.getAffinityScore() != null) {
            baseWeight += affinity.getAffinityScore();
        }
        return baseWeight;
    }
}
