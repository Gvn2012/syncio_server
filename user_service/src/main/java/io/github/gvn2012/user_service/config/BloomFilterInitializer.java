package io.github.gvn2012.user_service.config;

import io.github.gvn2012.user_service.services.interfaces.IBloomFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInitializer implements CommandLineRunner {

    private final IBloomFilterService bloomFilterService;
    private final RedissonClient redissonClient;

    private static final String LOCK_KEY = "lock:bf_init";

    @Override
    public void run(String... args) {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            if (lock.tryLock(10, 300, TimeUnit.SECONDS)) {
                try {
                    bloomFilterService.initialize();
                } finally {
                    lock.unlock();
                }
            } else {
                log.info("Another instance is already initializing Bloom filters.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Bloom filter initialization interrupted", e);
        }
    }
}
