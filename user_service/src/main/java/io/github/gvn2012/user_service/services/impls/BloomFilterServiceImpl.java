package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.repositories.UserEmailRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IBloomFilterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BloomFilterServiceImpl implements IBloomFilterService {

    private final RedissonClient redissonClient;
    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;

    private static final String USERNAME_FILTER = "bf:usernames";
    private static final String EMAIL_FILTER = "bf:emails";

    private static final long EXPECTED_INSERTIONS = 1000000L;
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;

    private RBloomFilter<String> usernameFilter;
    private RBloomFilter<String> emailFilter;

    @PostConstruct
    public void init() {
        usernameFilter = redissonClient.getBloomFilter(USERNAME_FILTER);
        usernameFilter.tryInit(EXPECTED_INSERTIONS, FALSE_POSITIVE_PROBABILITY);

        emailFilter = redissonClient.getBloomFilter(EMAIL_FILTER);
        emailFilter.tryInit(EXPECTED_INSERTIONS, FALSE_POSITIVE_PROBABILITY);

        log.info("Bloom filters initialized.");
    }

    @Override
    public boolean mightContainUsername(String username) {
        return usernameFilter.contains(username);
    }

    @Override
    public void addUsername(String username) {
        usernameFilter.add(username);
    }

    @Override
    public boolean mightContainEmail(String email) {
        return emailFilter.contains(email);
    }

    @Override
    public void addEmail(String email) {
        emailFilter.add(email);
    }

    private static final String INITIALIZED_FLAG = "bf:initialized";

    @Override
    public void initialize() {
        if (Boolean.TRUE.equals(redissonClient.getBucket(INITIALIZED_FLAG).get())) {
            log.info("Bloom filters already populated. Skipping...");
            return;
        }

        log.info("Starting Bloom filter population from database...");

        userRepository.findAll().forEach(user -> {
            if (user.getUsername() != null) {
                addUsername(user.getUsername());
            }
        });

        userEmailRepository.findAll().forEach(userEmail -> {
            if (userEmail.getEmail() != null) {
                addEmail(userEmail.getEmail());
            }
        });

        redissonClient.getBucket(INITIALIZED_FLAG).set(true);
        log.info("Bloom filter population completed.");
    }
}
