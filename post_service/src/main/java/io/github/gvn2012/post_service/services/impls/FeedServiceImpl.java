package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.repositories.ContentRankingRepository;
import io.github.gvn2012.post_service.repositories.FeedItemRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private final FeedItemRepository feedItemRepository;
    private final ContentRankingRepository contentRankingRepository;
    private final PostRepository postRepository;
    private final FeedRankingService rankingService;
    private final RelationshipClient relationshipClient;

    @Override
    public List<Post> getHybridFeed(UUID recipientId, LocalDateTime cursor, int limit) {
        List<Post> candidates = new ArrayList<>();

        List<FeedItem> pushedItems = feedItemRepository.findByRecipientIdOrderByWeightScoreDescCreatedAtDesc(
                recipientId, PageRequest.of(0, limit * 2));
        pushedItems.forEach(item -> candidates.add(item.getSourcePost()));

        List<UUID> highVolumeFollows = resolveHighVolumeFollows(recipientId);
        if (!highVolumeFollows.isEmpty()) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
            List<ContentRanking> pulledRankings = contentRankingRepository.findTopRankingsByAuthors(
                    highVolumeFollows, cutoff, PageRequest.of(0, limit));
            pulledRankings.forEach(ranking -> candidates.add(ranking.getPost()));
        }

        List<Post> pinnedPosts = postRepository.findPinnedAndPublishedAnnouncements(PageRequest.of(0, 5));
        candidates.addAll(pinnedPosts);

        Map<UUID, Post> deduped = new LinkedHashMap<>();
        for (Post post : candidates) {
            deduped.putIfAbsent(post.getId(), post);
        }

        return deduped.values().stream()
                .filter(this::isVisible)
                .sorted((a, b) -> {
                    double scoreA = rankingService.computeScore(a, recipientId);
                    double scoreB = rankingService.computeScore(b, recipientId);
                    return Double.compare(scoreB, scoreA);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getTrendingPosts(LocalDateTime since, int limit) {
        List<ContentRanking> rankings = contentRankingRepository.findTopRankingsSince(
                since, PageRequest.of(0, limit));
        return rankings.stream()
                .map(ContentRanking::getPost)
                .filter(this::isVisible)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getFollowingFeed(UUID recipientId, LocalDateTime cursor, int limit) {
        List<UUID> followedIds = resolveFollows(recipientId);
        if (followedIds.isEmpty())
            return List.of();
        return postRepository.findByAuthorIdInAndStatusOrderByPublishedAtDesc(
                followedIds, PostStatus.PUBLISHED, PageRequest.of(0, limit));
    }

    private List<UUID> resolveHighVolumeFollows(UUID userId) {
        try {
            // High volume follows logic usually involves a specific flag or threshold
            // For now, we use getFollowing as it's the intended source for feed content
            return relationshipClient.getFollowing(userId)
                    .onErrorReturn(List.of())
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<UUID> resolveFollows(UUID userId) {
        try {
            return relationshipClient.getFollowing(userId)
                    .onErrorReturn(List.of())
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean isVisible(Post post) {
        return post.getStatus() == PostStatus.PUBLISHED
                && post.getModerationStatus() != PostModerationStatus.REMOVED
                && post.getModerationStatus() != PostModerationStatus.RESTRICTED;
    }
}
