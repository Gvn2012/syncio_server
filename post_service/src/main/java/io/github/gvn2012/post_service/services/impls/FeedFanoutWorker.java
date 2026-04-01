package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import io.github.gvn2012.post_service.repositories.FeedItemRepository;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedFanoutWorker {

    private static final int FANOUT_THRESHOLD = 50_000;

    private final FeedItemRepository feedItemRepository;
    private final UserAffinityRepository affinityRepository;
    private final RelationshipClient relationshipClient;

    public record PostCreatedEvent(Post post) {}

    @Async
    @TransactionalEventListener
    public void handlePostCreated(PostCreatedEvent event) {
        Post post = event.post();
        List<UUID> followers = lookupFollowers(post.getAuthorId());

        if (followers.size() > FANOUT_THRESHOLD) {
            log.info("Skipping fanout for high-volume author {} ({} followers)", post.getAuthorId(), followers.size());
            return;
        }

        List<FeedItem> feedItems = followers.stream()
                .filter(followerId -> canViewPost(post, followerId))
                .map(followerId -> {
                    FeedItem item = new FeedItem();
                    item.setRecipientId(followerId);
                    item.setSourcePost(post);
                    item.setOrgId(post.getOrgId());
                    item.setWeightScore(calculateInitialWeight(post, followerId));
                    item.setReasonCode("follower");
                    item.setIsRead(false);
                    item.setIsHidden(false);
                    return item;
                }).toList();

        if (!feedItems.isEmpty()) {
            feedItemRepository.saveAll(feedItems);
            log.info("Fan-out {} feed items for post {}", feedItems.size(), post.getId());
        }
    }

    private List<UUID> lookupFollowers(UUID authorId) {
        try {
            List<UUID> followers = relationshipClient.getFollowers(authorId).block();
            return followers != null ? followers : List.of();
        } catch (Exception e) {
            log.warn("Failed to fetch followers for {}: {}", authorId, e.getMessage());
            return List.of();
        }
    }

    private boolean canViewPost(Post post, UUID recipientId) {
        if (post.getVisibility() == PostVisibility.PUBLIC || post.getVisibility() == PostVisibility.COMPANY) {
            return true;
        }
        if (post.getVisibility() == PostVisibility.FOLLOWER) {
            return true;
        }
        return post.getVisibility() != PostVisibility.PRIVATE;
    }

    private double calculateInitialWeight(Post post, UUID recipientId) {
        double baseWeight = 1.0;
        try {
            baseWeight += affinityRepository.findByUserIdAndAuthorId(recipientId, post.getAuthorId())
                    .map(UserAffinity::getAffinityScore)
                    .orElse(0.0);
        } catch (Exception e) {
            log.debug("Affinity lookup failed for user {}: {}", recipientId, e.getMessage());
        }
        return baseWeight;
    }
}
