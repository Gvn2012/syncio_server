package io.github.gvn2012.messaging_service.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PRESENCE_KEY_PREFIX = "user:status:";
    private static final long ONLINE_TIMEOUT = 5;

    public void setUserOnline(String userId) {
        redisTemplate.opsForValue().set(PRESENCE_KEY_PREFIX + userId, "ONLINE", ONLINE_TIMEOUT, TimeUnit.MINUTES);
    }

    public void setUserOffline(String userId) {
        redisTemplate.delete(PRESENCE_KEY_PREFIX + userId);
    }

    public boolean isUserOnline(String userId) {
        return redisTemplate.hasKey(PRESENCE_KEY_PREFIX + userId);
    }
}
