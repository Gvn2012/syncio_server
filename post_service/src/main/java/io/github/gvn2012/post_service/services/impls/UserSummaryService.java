package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.UserClient;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSummaryService {

    private final UserClient userClient;
    private final RedisTemplate<String, UserSummaryResponse> userSummaryRedisTemplate;

    private static final String CACHE_PREFIX = "user:summary:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    public Map<UUID, UserSummaryResponse> getSummaries(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<UUID, UserSummaryResponse> result = new HashMap<>();
        Set<UUID> missingIds = new HashSet<>();

        try {
            for (UUID userId : userIds) {
                String key = CACHE_PREFIX + userId.toString();
                UserSummaryResponse cached = userSummaryRedisTemplate.opsForValue().get(key);
                if (cached != null) {
                    result.put(userId, cached);
                } else {
                    missingIds.add(userId);
                }
            }
        } catch (Exception e) {
            log.warn("Redis lookup failed, falling back to direct service fetch. Error: {}", e.getMessage());
            missingIds.addAll(userIds); // Fetch everything from service if Redis fails
        }

        if (missingIds.isEmpty()) {
            return result;
        }
        log.info("Fetching {} users from user-service", missingIds.size());
        try {
            Map<UUID, UserSummaryResponse> freshSummaries = userClient.getUsersSummaries(missingIds)
                    .block(Duration.ofSeconds(5));
            if (freshSummaries != null) {
                freshSummaries.forEach((id, summary) -> {
                    result.put(id, summary);
                    try {
                        String key = CACHE_PREFIX + id.toString();
                        userSummaryRedisTemplate.opsForValue().set(key, summary, CACHE_TTL);
                    } catch (Exception e) {
                        log.warn("Failed to update Redis cache: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to fetch user summaries from user-service", e);
        }

        return result;
    }

    public UserSummaryResponse getSummary(UUID userId) {
        return getSummaries(Collections.singleton(userId)).get(userId);
    }
}
