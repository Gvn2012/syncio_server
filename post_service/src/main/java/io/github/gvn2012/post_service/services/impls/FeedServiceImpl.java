package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RankingClient;
import io.github.gvn2012.post_service.clients.RelationshipClient;
import io.github.gvn2012.post_service.dtos.requests.RankingRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.RankingResponseDTO;
import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import lombok.extern.slf4j.Slf4j;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
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
import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    private final IInteractionVelocityService velocityService;
    private final RankingClient rankingClient;
    private final RedisTemplate<String, String> interactionRedisTemplate;

    private static final String RANKED_FEED_CACHE_PREFIX = "feed:ranked:";
    private static final int RANKED_POOL_SIZE = 200;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    public FeedServiceImpl(
            FeedItemRepository feedItemRepository,
            ContentRankingRepository contentRankingRepository,
            PostRepository postRepository,
            FeedRankingService rankingService,
            RelationshipClient relationshipClient,
            UserAffinityRepository userAffinityRepository,
            UserSummaryService userSummaryService,
            PostReactionRepository postReactionRepository,
            PostMapper postMapper,
            IInteractionVelocityService velocityService,
            RankingClient rankingClient,
            RedisTemplate<String, String> interactionRedisTemplate) {
        this.feedItemRepository = feedItemRepository;
        this.contentRankingRepository = contentRankingRepository;
        this.postRepository = postRepository;
        this.rankingService = rankingService;
        this.relationshipClient = relationshipClient;
        this.userAffinityRepository = userAffinityRepository;
        this.userSummaryService = userSummaryService;
        this.postReactionRepository = postReactionRepository;
        this.postMapper = postMapper;
        this.velocityService = velocityService;
        this.rankingClient = rankingClient;
        this.interactionRedisTemplate = interactionRedisTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getHybridFeed(UUID recipientId, LocalDateTime cursor, int limit) {
        String cacheKey = RANKED_FEED_CACHE_PREFIX + recipientId.toString();
        String lastTsKey = cacheKey + ":last_ts";

        // 1. Initial Load
        if (cursor == null) {
            return generateAndCacheRankedFeed(recipientId, limit, cacheKey, null);
        }

        // 2. Check if we need a Refill (Smart Refill Trigger)
        String lastTsStr = interactionRedisTemplate.opsForValue().get(lastTsKey);
        if (lastTsStr != null) {
            LocalDateTime lastTs = LocalDateTime.parse(lastTsStr);
            if (cursor.isBefore(lastTs) || cursor.isEqual(lastTs)) {
                log.info("Smart Refill triggered for user {}. Cursor {} is beyond cached window {}.", 
                        recipientId, cursor, lastTs);
                return generateAndCacheRankedFeed(recipientId, limit, cacheKey, cursor);
            }
        }

        // 3. Fallback/Standard Page Retrieval
        return retrieveFreshHybridPage(recipientId, cursor, limit);
    }

    private List<PostResponse> generateAndCacheRankedFeed(UUID recipientId, int limit, String cacheKey, LocalDateTime cursor) {
        LocalDateTime effectiveCursor = (cursor != null) ? cursor : LocalDateTime.now();
        List<PostCategory> excludedCategories = List.of(PostCategory.TASK, PostCategory.ANNOUNCEMENT);

        List<Post> candidates = new ArrayList<>();

        List<UUID> followedIds = resolveFollows(recipientId);
        List<UUID> blockedIds = resolveBlocks(recipientId);
        List<UUID> blockedByIds = resolveBlockedBy(recipientId);
        Set<UUID> allBlocked = new HashSet<>(blockedIds);
        allBlocked.addAll(blockedByIds);

        // 2. Fetch Pushed Content (Fan-out Inbox) - Fetch more for a healthy pool
        List<FeedItem> pushedItems = feedItemRepository.findByRecipientIdAndCursor(
                recipientId, effectiveCursor, excludedCategories, PageRequest.of(0, RANKED_POOL_SIZE / 2));
        pushedItems.forEach(item -> candidates.add(item.getSourcePost()));

        // 3. Fetch Pulled Content (Top-K from followed users)
        if (!followedIds.isEmpty()) {
            List<ContentRanking> pulledRankings = contentRankingRepository.findTopRankingsByAuthorsAndCursor(
                    followedIds, effectiveCursor, excludedCategories, PageRequest.of(0, RANKED_POOL_SIZE / 2));
            pulledRankings.forEach(ranking -> candidates.add(ranking.getPost()));
        }

        // 4. Global Discovery Content
        List<ContentRanking> discoveryRankings = contentRankingRepository.findGlobalTopRankings(
                effectiveCursor, excludedCategories, PageRequest.of(0, 50));

        discoveryRankings.stream()
                .map(ContentRanking::getPost)
                .filter(post -> post.getVisibility() == PostVisibility.PUBLIC)
                .filter(post -> !followedIds.contains(post.getAuthorId()))
                .filter(post -> !allBlocked.contains(post.getAuthorId()))
                .filter(post -> !post.getAuthorId().equals(recipientId))
                .forEach(candidates::add);

        // 5. Deduplicate
        Map<UUID, Post> deduped = new LinkedHashMap<>();
        for (Post post : candidates) {
            deduped.putIfAbsent(post.getId(), post);
        }

        // 6. Feature Engineering for ML
        List<UUID> poolAuthorIds = deduped.values().stream().map(Post::getAuthorId).distinct().toList();
        Map<UUID, UserAffinity> affinities = userAffinityRepository
                .findByUserIdAndAuthorIdIn(recipientId, poolAuthorIds)
                .stream()
                .collect(Collectors.toMap(UserAffinity::getAuthorId, a -> a));

        List<RankingRequestDTO.PostFeatureDTO> features = deduped.values().stream()
                .map(post -> RankingRequestDTO.PostFeatureDTO.builder()
                        .postId(post.getId())
                        .authorId(post.getAuthorId())
                        .authorAffinity(affinities.containsKey(post.getAuthorId())
                                ? affinities.get(post.getAuthorId()).getAffinityScore()
                                : 0.0)
                        .velocityScore(velocityService.getVelocityScore(post.getId()))
                        .recencyHours((double) Duration
                                .between(post.getPublishedAt() != null ? post.getPublishedAt() : post.getCreatedAt(),
                                        effectiveCursor)
                                .toHours())
                        .category(post.getPostCategory().name())
                        .mediaCount(post.getAttachments() != null ? post.getAttachments().size() : 0)
                        .build())
                .collect(Collectors.toList());

        // 7. ML Ranking
        RankingRequestDTO rankingRequest = RankingRequestDTO.builder()
                .userId(recipientId)
                .candidates(features)
                .build();

        RankingResponseDTO rankingResponse = rankingClient.rankPosts(rankingRequest).block();

        List<UUID> sortedIds = new ArrayList<>();
        if (rankingResponse != null && rankingResponse.getRankedCandidates() != null) {
            sortedIds = rankingResponse.getRankedCandidates().stream()
                    .map(RankingResponseDTO.RankedPostDTO::getPostId)
                    .collect(Collectors.toList());
        } else {
            // Local fallback sorting
            sortedIds = deduped.values().stream()
                    .sorted((a, b) -> {
                        UserAffinity aff = affinities.get(b.getAuthorId());
                        Double scoreB = rankingService.computeScore(b, recipientId,
                                aff != null ? aff.getAffinityScore() : null);
                        Double scoreA = rankingService.computeScore(a, recipientId,
                                affinities.get(a.getAuthorId()) != null
                                        ? affinities.get(a.getAuthorId()).getAffinityScore()
                                        : null);
                        return scoreB.compareTo(scoreA);
                    })
                    .map(Post::getId)
                    .collect(Collectors.toList());
        }

        // 8. Cache the sorted IDs in Redis (Append if refilling, Replace if fresh)
        if (cursor == null) {
            interactionRedisTemplate.delete(cacheKey);
            interactionRedisTemplate.delete(cacheKey + ":last_ts");
        }

        if (!sortedIds.isEmpty()) {
            List<String> idStrings = sortedIds.stream().map(UUID::toString).toList();
            interactionRedisTemplate.opsForList().rightPushAll(cacheKey, idStrings);
            interactionRedisTemplate.expire(cacheKey, CACHE_TTL);

            // Update the "Last Timestamp" in this batch to detect future refills
            UUID lastIdInBatch = sortedIds.get(sortedIds.size() - 1);
            Post lastPost = deduped.get(lastIdInBatch);
            if (lastPost != null) {
                LocalDateTime lastPostTs = lastPost.getPublishedAt() != null ? lastPost.getPublishedAt() : lastPost.getCreatedAt();
                interactionRedisTemplate.opsForValue().set(cacheKey + ":last_ts", lastPostTs.toString(), CACHE_TTL);
            }
        }

        // 9. Diversity Filter & Return Page 1
        List<Post> topPosts = sortedIds.stream()
                .map(deduped::get)
                .filter(Objects::nonNull)
                .limit(limit)
                .toList();

        return enrichPosts(topPosts, recipientId);
    }

    private List<PostResponse> retrieveFreshHybridPage(UUID recipientId, LocalDateTime cursor, int limit) {
        // This is the fallback/standard logic for subsequent pages if cache is missing
        // Fetches from DB based on cursor...
        List<PostCategory> excludedCategories = List.of(PostCategory.TASK, PostCategory.ANNOUNCEMENT);
        List<FeedItem> pushedItems = feedItemRepository.findByRecipientIdAndCursor(
                recipientId, cursor, excludedCategories, PageRequest.of(0, limit));

        List<Post> results = pushedItems.stream().map(FeedItem::getSourcePost).collect(Collectors.toList());
        return enrichPosts(results, recipientId);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
