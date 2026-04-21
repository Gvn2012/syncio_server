package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import io.github.gvn2012.post_service.dtos.mappers.PostMapper;
import io.github.gvn2012.post_service.repositories.ContentRankingRepository;
import io.github.gvn2012.post_service.repositories.FeedItemRepository;
import io.github.gvn2012.post_service.repositories.PostReactionRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.repositories.UserAffinityRepository;
import io.github.gvn2012.post_service.services.interfaces.IFeedService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl implements IFeedService {

    private final FeedItemRepository feedItemRepository;
    private final ContentRankingRepository contentRankingRepository;
    private final PostRepository postRepository;
    private final FeedRankingService rankingService;
    private final RelationshipClient relationshipClient;
    private final UserAffinityRepository userAffinityRepository;
    private final UserSummaryService userSummaryService;
    private final PostReactionRepository postReactionRepository;
    private final PostMapper postMapper;

    public FeedServiceImpl(
            FeedItemRepository feedItemRepository,
            ContentRankingRepository contentRankingRepository,
            PostRepository postRepository,
            FeedRankingService rankingService,
            RelationshipClient relationshipClient,
            UserAffinityRepository userAffinityRepository,
            UserSummaryService userSummaryService,
            PostReactionRepository postReactionRepository,
            PostMapper postMapper) {
        this.feedItemRepository = feedItemRepository;
        this.contentRankingRepository = contentRankingRepository;
        this.postRepository = postRepository;
        this.rankingService = rankingService;
        this.relationshipClient = relationshipClient;
        this.userAffinityRepository = userAffinityRepository;
        this.userSummaryService = userSummaryService;
        this.postReactionRepository = postReactionRepository;
        this.postMapper = postMapper;
    }

    @Override
    public List<PostResponse> getHybridFeed(UUID recipientId, LocalDateTime cursor, int limit) {
        LocalDateTime effectiveCursor = (cursor != null) ? cursor : LocalDateTime.now();
        List<PostCategory> excludedCategories = List.of(PostCategory.TASK, PostCategory.ANNOUNCEMENT);

        List<Post> candidates = new ArrayList<>();

        // 1. Resolve Relationships (Follows & Blocks)
        List<UUID> followedIds = resolveFollows(recipientId);
        List<UUID> blockedIds = resolveBlocks(recipientId);
        List<UUID> blockedByIds = resolveBlockedBy(recipientId);
        Set<UUID> allBlocked = new HashSet<>(blockedIds);
        allBlocked.addAll(blockedByIds);

        // 2. Fetch Pushed Content (Inbox / Fan-out)
        List<FeedItem> pushedItems = feedItemRepository.findByRecipientIdAndCursor(
                recipientId, effectiveCursor, excludedCategories, PageRequest.of(0, limit * 2));
        pushedItems.forEach(item -> candidates.add(item.getSourcePost()));

        // 3. Fetch Pulled Content (Top content from followed users)
        if (!followedIds.isEmpty()) {
            List<ContentRanking> pulledRankings = contentRankingRepository.findTopRankingsByAuthorsAndCursor(
                    followedIds, effectiveCursor, excludedCategories, PageRequest.of(0, limit));
            pulledRankings.forEach(ranking -> candidates.add(ranking.getPost()));
        }

        // 4. Global Discovery Content (Randomness from non-followed authors)
        List<ContentRanking> discoveryRankings = contentRankingRepository.findGlobalTopRankings(
                effectiveCursor, excludedCategories, PageRequest.of(0, limit));

        discoveryRankings.stream()
                .map(ContentRanking::getPost)
                .filter(post -> post.getVisibility() == PostVisibility.PUBLIC)
                .filter(post -> !followedIds.contains(post.getAuthorId()))
                .filter(post -> !allBlocked.contains(post.getAuthorId()))
                .filter(post -> !post.getAuthorId().equals(recipientId))
                .forEach(candidates::add);

        // 5. Deduplicate by Post ID
        Map<UUID, Post> deduped = new LinkedHashMap<>();
        for (Post post : candidates) {
            deduped.putIfAbsent(post.getId(), post);
        }

        // 6. Enrichment with User Affinity
        List<UUID> poolAuthorIds = deduped.values().stream().map(Post::getAuthorId).distinct().toList();
        Map<UUID, UserAffinity> affinities = userAffinityRepository
                .findByUserIdAndAuthorIdIn(recipientId, poolAuthorIds)
                .stream()
                .collect(Collectors.toMap(UserAffinity::getAuthorId, a -> a));

        // 7. Scoring & Jitter (Variability)
        Random random = new Random();
        Map<UUID, Double> scores = new HashMap<>();
        for (Post post : deduped.values()) {
            UserAffinity aff = affinities.get(post.getAuthorId());
            Double affinityScore = (aff != null) ? aff.getAffinityScore() : null;
            double baseScore = rankingService.computeScore(post, recipientId, affinityScore);

            if (!followedIds.contains(post.getAuthorId()) && !post.getAuthorId().equals(recipientId)) {
                baseScore *= 0.95;
            }

            double jitter = 1.0 + (random.nextDouble() * 0.02);
            scores.put(post.getId(), baseScore * jitter);
        }

        // 8. Sorting & Author Capping (Diversity Filter)
        Map<UUID, Integer> authorCounts = new HashMap<>();
        int MAX_POSTS_PER_AUTHOR = 2;

        List<Post> sortedPosts = deduped.values().stream()
                .filter(this::isVisible)
                .filter(post -> !allBlocked.contains(post.getAuthorId()))
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)))
                .filter(post -> {
                    int count = authorCounts.getOrDefault(post.getAuthorId(), 0);
                    if (count < MAX_POSTS_PER_AUTHOR) {
                        authorCounts.put(post.getAuthorId(), count + 1);
                        return true;
                    }
                    return false;
                })
                .limit(limit)
                .collect(Collectors.toList());

        return enrichPosts(sortedPosts, recipientId);
    }

    @Override
    public List<PostResponse> getTrendingPosts(UUID viewerId, LocalDateTime since, int limit) {
        List<ContentRanking> rankings = contentRankingRepository.findTopRankingsSince(
                since, PageRequest.of(0, limit));
        List<Post> posts = rankings.stream()
                .map(ContentRanking::getPost)
                .filter(this::isVisible)
                .collect(Collectors.toList());
        return enrichPosts(posts, viewerId);
    }

    @Override
    public List<PostResponse> getFollowingFeed(UUID recipientId, LocalDateTime cursor, int limit) {
        List<UUID> followedIds = resolveFollows(recipientId);
        if (followedIds.isEmpty())
            return List.of();
        List<Post> posts = postRepository.findByAuthorIdInAndStatusOrderByPublishedAtDesc(
                followedIds, PostStatus.PUBLISHED, PageRequest.of(0, limit));
        return enrichPosts(posts, recipientId);
    }

    private List<PostResponse> enrichPosts(List<Post> posts, UUID viewerId) {
        if (posts == null || posts.isEmpty())
            return List.of();

        Set<UUID> postIds = posts.stream().map(Post::getId).collect(Collectors.toSet());
        Set<UUID> authorIds = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());

        Map<UUID, UserSummaryResponse> authorSummaries = userSummaryService.getSummaries(authorIds);

        Map<UUID, String> reactionsMap = new HashMap<>();
        Set<UUID> sharedPostIds = new HashSet<>();

        if (viewerId != null) {
            postReactionRepository.findByUserIdAndPostIdIn(viewerId, postIds)
                    .forEach(r -> reactionsMap.put(r.getPost().getId(), r.getReactionType().getCode()));

            sharedPostIds = postRepository.findSharedPostIdsByAuthor(viewerId, postIds);
        }

        final Set<UUID> sharedIdsFinal = sharedPostIds;

        return posts.stream().map(post -> {
            PostResponse response = postMapper.toResponse(post);
            response.setAuthorInfo(authorSummaries.get(post.getAuthorId()));

            if (viewerId != null) {
                response.setViewerReaction(reactionsMap.get(post.getId()));
                response.setSharedByViewer(sharedIdsFinal.contains(post.getId()));
            }
            return response;
        }).collect(Collectors.toList());
    }

    private List<UUID> resolveFollows(UUID userId) {
        try {
            return relationshipClient.getFollowing(userId)
                    .timeout(java.time.Duration.ofMillis(3000))
                    .onErrorReturn(List.of())
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<UUID> resolveBlocks(UUID userId) {
        try {
            return relationshipClient.getBlockedList(userId)
                    .timeout(java.time.Duration.ofMillis(3000))
                    .onErrorReturn(List.of())
                    .block();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<UUID> resolveBlockedBy(UUID userId) {
        try {
            return relationshipClient.getBlockedByList(userId)
                    .timeout(java.time.Duration.ofMillis(3000))
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
