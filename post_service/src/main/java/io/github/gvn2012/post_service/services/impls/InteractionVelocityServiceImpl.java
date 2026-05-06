package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.services.interfaces.IInteractionVelocityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<UUID, Double> getVelocityScores(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<Object> results = interactionRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (UUID postId : postIds) {
                    connection.zSetCommands().zScore(TRENDING_KEY.getBytes(), postId.toString().getBytes());
                }
                return null;
            });

            Map<UUID, Double> scores = new HashMap<>();
            int i = 0;
            for (UUID postId : postIds) {
                Object result = results.get(i++);
                scores.put(postId, result instanceof Double ? (Double) result : 0.0);
            }
            return scores;
        } catch (Exception e) {
            log.error("Failed to fetch batch velocity scores from Redis", e);
            return postIds.stream().collect(Collectors.toMap(id -> id, id -> 0.0));
        }
    }
}
