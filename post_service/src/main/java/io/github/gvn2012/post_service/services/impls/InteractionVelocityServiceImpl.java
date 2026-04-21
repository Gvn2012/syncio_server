package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InteractionVelocityServiceImpl implements IInteractionVelocityService {

    private final RedisTemplate<String, String> interactionRedisTemplate;
    private static final String TRENDING_KEY = "post:trending:velocity";

    @Override
    public void recordInteraction(UUID postId, InteractionType type) {
        try {
            interactionRedisTemplate.opsForZSet().incrementScore(TRENDING_KEY, postId.toString(), type.getWeight());
            log.debug("Recorded interaction of type {} for post {}", type, postId);
        } catch (Exception e) {
            log.error("Failed to record interaction in Redis for post: {}", postId, e);
        }
    }

    @Override
    public List<UUID> getTrendingPosts(int limit) {
        try {
            Set<String> postIds = interactionRedisTemplate.opsForZSet().reverseRange(TRENDING_KEY, 0, limit - 1);
            if (postIds == null || postIds.isEmpty()) {
                return Collections.emptyList();
            }
            return postIds.stream().map(UUID::fromString).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch trending posts from Redis", e);
            return Collections.emptyList();
        }
    }

    @Override
    public double getVelocityScore(UUID postId) {
        try {
            Double score = interactionRedisTemplate.opsForZSet().score(TRENDING_KEY, postId.toString());
            return score != null ? score : 0.0;
        } catch (Exception e) {
            log.error("Failed to get velocity score for post: {}", postId, e);
            return 0.0;
        }
    }
}
