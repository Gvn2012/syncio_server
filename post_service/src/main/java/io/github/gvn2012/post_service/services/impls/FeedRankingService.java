package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedRankingService {

    private static final double W_RECENCY = 0.45;
    private static final double W_ENGAGEMENT = 0.30;
    private static final double W_AFFINITY = 0.15;
    private static final double W_TYPE = 0.05;
    private static final double W_BUSINESS = 0.05;
    private static final double W_DEMOTE = 1.0;

    private final UserAffinityRepository affinityRepository;

    public double computeScore(Post post, UUID viewerId) {
        double recency = computeRecencyScore(post);
        double engagement = computeEngagementScore(post);
        double affinity = computeAffinityScore(post.getAuthorId(), viewerId, post.getOrgId());
        double typeBoost = computeTypeBoost(post);
        double businessBoost = computeBusinessBoost(post);
        double demotion = computeDemotionPenalty(post);

        return W_RECENCY * recency
                + W_ENGAGEMENT * Math.log1p(engagement)
                + W_AFFINITY * affinity
                + W_TYPE * typeBoost
                + W_BUSINESS * businessBoost
                - W_DEMOTE * demotion;
    }

    public double computeScoreFromRanking(ContentRanking ranking, UUID viewerId) {
        Post post = ranking.getPost();
        double recency = computeRecencyScore(post);
        double affinity = computeAffinityScore(post.getAuthorId(), viewerId, post.getOrgId());
        double typeBoost = computeTypeBoost(post);
        double businessBoost = ranking.getBusinessBoost();

        return W_RECENCY * recency
                + W_ENGAGEMENT * Math.log1p(ranking.getEngagementScore())
                + W_AFFINITY * affinity
                + W_TYPE * typeBoost
                + W_BUSINESS * businessBoost;
    }

    private double computeRecencyScore(Post post) {
        LocalDateTime published = post.getPublishedAt() != null ? post.getPublishedAt() : post.getCreatedAt();
        if (published == null) return 0.5;
        long hours = Duration.between(published, LocalDateTime.now()).toHours();
        return 1.0 / (1.0 + hours);
    }

    private double computeEngagementScore(Post post) {
        return 0.3 * post.getReactionCount()
                + 0.3 * post.getCommentCount()
                + 0.25 * post.getShareCount()
                + 0.15 * post.getViewCount().doubleValue();
    }

    private double computeAffinityScore(UUID authorId, UUID viewerId, UUID orgId) {
        if (authorId.equals(viewerId)) return 1.0;
        Optional<UserAffinity> affinity = affinityRepository.findByUserIdAndAuthorId(viewerId, authorId);
        return affinity.map(UserAffinity::getAffinityScore).orElse(0.0);
    }

    private double computeTypeBoost(Post post) {
        return switch (post.getPostCategory()) {
            case NORMAL -> 1.0;
            case EVENT -> 1.1;
            case POLL -> 1.1;
            case TASK -> 1.0;
            case ANNOUNCEMENT -> 1.5;
        };
    }

    private double computeBusinessBoost(Post post) {
        if (post.getPostCategory() == PostCategory.ANNOUNCEMENT) return 2.0;
        return 0.0;
    }

    private double computeDemotionPenalty(Post post) {
        PostModerationStatus status = post.getModerationStatus();
        if (status == null || status == PostModerationStatus.NONE) return 0.0;
        return switch (status) {
            case FLAGGED -> 0.2;
            case REPORTED -> 0.3;
            case UNDER_REVIEW -> 0.5;
            case RESTRICTED -> 0.8;
            case REMOVED -> 1.0;
            case NONE -> 0.0;
        };
    }
}
