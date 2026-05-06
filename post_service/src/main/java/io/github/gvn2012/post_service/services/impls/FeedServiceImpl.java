package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RankingClient;
import io.github.gvn2012.post_service.dtos.requests.RankingRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.RankingResponseDTO;
import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.FeedItem;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.UserAffinity;
import io.github.gvn2012.post_service.entities.enums.ReactionType;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FeedServiceImpl implements IFeedService {

    private final FeedItemRepository feedItemRepository;
    private final ContentRankingRepository contentRankingRepository;
    private final PostRepository postRepository;
    private final FeedRankingService rankingService;
    private final UserAffinityRepository userAffinityRepository;
    private final UserSummaryService userSummaryService;
    private final PostReactionRepository postReactionRepository;
    private final PostMapper postMapper;
    private final IInteractionVelocityService velocityService;
    private final SocialRelationshipService socialRelationshipService;
    private final RankingClient rankingClient;
    private final RedisTemplate<String, String> interactionRedisTemplate;
    private final MediaEnrichmentService mediaEnrichmentService;

    private static final String RANKED_FEED_CACHE_PREFIX = "feed:ranked:";
    private static final int RANKED_POOL_SIZE = 200;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    public FeedServiceImpl(
            FeedItemRepository feedItemRepository,
            ContentRankingRepository contentRankingRepository,
            PostRepository postRepository,
            FeedRankingService rankingService,
            UserAffinityRepository userAffinityRepository,
            UserSummaryService userSummaryService,
            PostReactionRepository postReactionRepository,
            PostMapper postMapper,
            IInteractionVelocityService velocityService,
            SocialRelationshipService socialRelationshipService,
            RankingClient rankingClient,
            RedisTemplate<String, String> interactionRedisTemplate,
            MediaEnrichmentService mediaEnrichmentService) {
        this.feedItemRepository = feedItemRepository;
        this.contentRankingRepository = contentRankingRepository;
        this.postRepository = postRepository;
        this.rankingService = rankingService;
        this.userAffinityRepository = userAffinityRepository;
        this.userSummaryService = userSummaryService;
        this.postReactionRepository = postReactionRepository;
        this.postMapper = postMapper;
        this.velocityService = velocityService;
        this.socialRelationshipService = socialRelationshipService;
        this.rankingClient = rankingClient;
        this.interactionRedisTemplate = interactionRedisTemplate;
        this.mediaEnrichmentService = mediaEnrichmentService;
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

    private List<PostResponse> generateAndCacheRankedFeed(UUID recipientId, int limit, String cacheKey,
            LocalDateTime cursor) {
        LocalDateTime effectiveCursor = (cursor != null) ? cursor : LocalDateTime.now();
        List<PostCategory> excludedCategories = List.of(PostCategory.TASK, PostCategory.ANNOUNCEMENT);

        List<Post> candidates = Collections.synchronizedList(new ArrayList<>());

        var followsFuture = CompletableFuture.supplyAsync(() -> resolveFollows(recipientId));
        var blocksFuture = CompletableFuture.supplyAsync(() -> resolveBlocks(recipientId));
        var blockedByFuture = CompletableFuture.supplyAsync(() -> resolveBlockedBy(recipientId));

        List<UUID> followedIds = followsFuture.join();
        List<UUID> blockedIds = blocksFuture.join();
        List<UUID> blockedByIds = blockedByFuture.join();
        Set<UUID> allBlocked = new HashSet<>(blockedIds);
        allBlocked.addAll(blockedByIds);

        // Parallel fetching of candidates
        CompletableFuture<Void> pushedTask = CompletableFuture.runAsync(() -> {
            List<FeedItem> pushedItems = feedItemRepository.findByRecipientIdAndCursor(
                    recipientId, effectiveCursor, excludedCategories, PageRequest.of(0, RANKED_POOL_SIZE / 2));
            pushedItems.stream()
                    .filter(item -> !item.getSourcePost().getAuthorId().equals(recipientId))
                    .forEach(item -> candidates.add(item.getSourcePost()));
        });

        CompletableFuture<Void> pulledTask = CompletableFuture.runAsync(() -> {
            if (!followedIds.isEmpty()) {
                List<ContentRanking> pulledRankings = contentRankingRepository.findTopRankingsByAuthorsAndCursor(
                        followedIds, effectiveCursor, excludedCategories, PageRequest.of(0, RANKED_POOL_SIZE / 2));
                pulledRankings.forEach(ranking -> candidates.add(ranking.getPost()));
            }
        });

        CompletableFuture<Void> discoveryTask = CompletableFuture
                .runAsync(() -> {
                    List<ContentRanking> discoveryRankings = contentRankingRepository.findGlobalTopRankings(
                            effectiveCursor, excludedCategories, PageRequest.of(0, 50));

                    discoveryRankings.stream()
                            .map(ContentRanking::getPost)
                            .filter(post -> post.getVisibility() == PostVisibility.PUBLIC)
                            .filter(post -> !followedIds.contains(post.getAuthorId()))
                            .filter(post -> !allBlocked.contains(post.getAuthorId()))
                            .forEach(candidates::add);
                });

        CompletableFuture<Void> directPullTask = CompletableFuture
                .runAsync(() -> {
                    if (!followedIds.isEmpty()) {
                        postRepository
                                .findByAuthorIdInAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                                        followedIds, PostStatus.PUBLISHED, excludedCategories, effectiveCursor,
                                        PageRequest.of(0, 50))
                                .stream()
                                .filter(post -> !post.getAuthorId().equals(recipientId))
                                .forEach(candidates::add);
                    }
                });

        CompletableFuture<Void> directDiscoveryTask = CompletableFuture
                .runAsync(() -> {
                    postRepository
                            .findByVisibilityAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                                    PostVisibility.PUBLIC, PostStatus.PUBLISHED, excludedCategories, effectiveCursor,
                                    PageRequest.of(0, 50))
                            .stream()
                            .filter(post -> !allBlocked.contains(post.getAuthorId()))
                            .filter(post -> !post.getAuthorId().equals(recipientId))
                            .forEach(candidates::add);
                });

        CompletableFuture
                .allOf(pushedTask, pulledTask, discoveryTask, directPullTask, directDiscoveryTask).join();

        // 5. Deduplicate and Filter
        Map<UUID, Post> deduped = new LinkedHashMap<>();
        for (Post post : candidates) {
            if (post != null && !post.getAuthorId().equals(recipientId) && post.getOrgId() == null) {
                deduped.putIfAbsent(post.getId(), post);
            }
        }

        // 6. Feature Engineering for ML
        List<UUID> poolAuthorIds = deduped.values().stream().map(Post::getAuthorId).distinct().toList();
        Map<UUID, UserAffinity> affinities = userAffinityRepository
                .findByUserIdAndAuthorIdIn(recipientId, poolAuthorIds)
                .stream()
                .collect(Collectors.toMap(UserAffinity::getAuthorId, a -> a));

        Map<UUID, Double> velocityScores = velocityService.getVelocityScores(deduped.keySet());

        List<RankingRequestDTO.PostFeatureDTO> features = deduped.values().stream()
                .map(post -> RankingRequestDTO.PostFeatureDTO.builder()
                        .postId(post.getId())
                        .authorId(post.getAuthorId())
                        .authorAffinity(affinities.containsKey(post.getAuthorId())
                                ? affinities.get(post.getAuthorId()).getAffinityScore()
                                : 0.0)
                        .velocityScore(velocityScores.getOrDefault(post.getId(), 0.0))
                        .recencyHours(Math.max(0.0, (double) Duration
                                .between(post.getPublishedAt() != null ? post.getPublishedAt() : post.getCreatedAt(),
                                        effectiveCursor)
                                .toHours()))
                        .category(post.getPostCategory().name())
                        .mediaCount(post.getAttachments() != null ? post.getAttachments().size() : 0)
                        .build())
                .collect(Collectors.toList());

        // 7. ML Ranking
        RankingRequestDTO rankingRequest = RankingRequestDTO.builder()
                .userId(recipientId)
                .candidates(features)
                .build();

        log.info("Ranking request: {}", rankingRequest);

        RankingResponseDTO rankingResponse = rankingClient.rankPosts(rankingRequest).block();

        List<UUID> sortedIds = new ArrayList<>();
        if (rankingResponse != null && rankingResponse.getRankedCandidates() != null) {
            sortedIds = rankingResponse.getRankedCandidates().stream()
                    .map(RankingResponseDTO.RankedPostDTO::getPostId)
                    .collect(Collectors.toList());
        } else {
            log.warn(
                    "Ranking service unavailable or failed for user {}. Falling back to local heuristic ranking service.",
                    recipientId);
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
                LocalDateTime lastPostTs = lastPost.getPublishedAt() != null ? lastPost.getPublishedAt()
                        : lastPost.getCreatedAt();
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
        // Fallback logic for subsequent pages if cache is missing or expired
        List<PostCategory> excludedCategories = List.of(PostCategory.TASK, PostCategory.ANNOUNCEMENT);
        List<Post> candidates = new ArrayList<>();

        List<UUID> followedIds = resolveFollows(recipientId);
        List<UUID> blockedIds = resolveBlocks(recipientId);
        List<UUID> blockedByIds = resolveBlockedBy(recipientId);
        Set<UUID> allBlocked = new HashSet<>(blockedIds);
        allBlocked.addAll(blockedByIds);

        // 1. Pushed (Inbox)
        feedItemRepository.findByRecipientIdAndCursor(recipientId, cursor, excludedCategories, PageRequest.of(0, limit))
                .stream()
                .filter(item -> !item.getSourcePost().getAuthorId().equals(recipientId))
                .forEach(item -> candidates.add(item.getSourcePost()));

        // 2. Pulled (Direct)
        if (candidates.size() < limit && !followedIds.isEmpty()) {
            postRepository.findByAuthorIdInAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                    followedIds, PostStatus.PUBLISHED, excludedCategories, cursor, PageRequest.of(0, limit))
                    .stream()
                    .filter(post -> !post.getAuthorId().equals(recipientId))
                    .forEach(candidates::add);
        }

        // 3. Discovery (Direct)
        if (candidates.size() < limit) {
            postRepository.findByVisibilityAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                    PostVisibility.PUBLIC, PostStatus.PUBLISHED, excludedCategories, cursor, PageRequest.of(0, limit))
                    .stream()
                    .filter(post -> !allBlocked.contains(post.getAuthorId()))
                    .filter(post -> !post.getAuthorId().equals(recipientId))
                    .forEach(candidates::add);
        }

        // Deduplicate and return
        List<Post> results = candidates.stream()
                .filter(Objects::nonNull)
                .filter(post -> post.getOrgId() == null)
                .filter(post -> !allBlocked.contains(post.getAuthorId()))
                .collect(Collectors.toMap(Post::getId, p -> p, (p1, p2) -> p1, LinkedHashMap::new))
                .values().stream()
                .limit(limit)
                .toList();

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
                followedIds, PostStatus.PUBLISHED, PageRequest.of(0, limit))
                .stream()
                .filter(post -> post.getOrgId() == null)
                .toList();
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
                    .forEach(r -> reactionsMap.put(r.getPost().getId(), r.getReactionType().name()));

            sharedPostIds = new HashSet<>(postRepository.findSharedPostIdsByAuthor(viewerId, postIds));
        }

        final Set<UUID> sharedIdsFinal = sharedPostIds;

        Map<UUID, List<String>> topReactionsMap = new HashMap<>();
        postReactionRepository.findReactionsByPostIdIn(postIds).forEach(row -> {
            UUID pid = (UUID) row[0];
            ReactionType type = (ReactionType) row[1];
            topReactionsMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(type.name());
        });

        topReactionsMap.forEach((pid, list) -> {
            Map<String, Long> counts = list.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            List<String> top3 = counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            topReactionsMap.put(pid, top3);
        });

        List<PostResponse> responses = posts.stream().map(post -> {
            PostResponse response = postMapper.toResponse(post);
            response.setAuthorInfo(authorSummaries.get(post.getAuthorId()));

            if (viewerId != null) {
                response.setViewerReaction(reactionsMap.get(post.getId()));
                response.setSharedByViewer(sharedIdsFinal.contains(post.getId()));
            }

            response.setTopReactions(topReactionsMap.getOrDefault(post.getId(), Collections.emptyList()));

            return response;
        }).collect(Collectors.toList());

        mediaEnrichmentService.enrichMediaUrls(responses);
        return responses;
    }

    private List<UUID> resolveFollows(UUID userId) {
        List<UUID> following = socialRelationshipService.getFollowingIds(userId);
        List<UUID> friends = socialRelationshipService.getFriendIds(userId);
        Set<UUID> all = new HashSet<>(following);
        all.addAll(friends);
        return new ArrayList<>(all);
    }

    private List<UUID> resolveBlocks(UUID userId) {
        return socialRelationshipService.getBlockedList(userId);
    }

    private List<UUID> resolveBlockedBy(UUID userId) {
        return socialRelationshipService.getBlockedByList(userId);
    }

    private boolean isVisible(Post post) {
        return post.getOrgId() == null
                && post.getStatus() == PostStatus.PUBLISHED
                && post.getModerationStatus() != PostModerationStatus.REMOVED
                && post.getModerationStatus() != PostModerationStatus.RESTRICTED;
    }
}
