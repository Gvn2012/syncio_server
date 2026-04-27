package io.github.gvn2012.search_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final RestTemplate restTemplate;

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.post.url}")
    private String postServiceUrl;

    public void triggerFullReindex() {
        log.info("Triggering full re-index from all services...");
        
        try {
            restTemplate.postForEntity(userServiceUrl + "/api/v1/users/internal/reindex", null, Void.class);
            log.info("Triggered user re-indexing.");
        } catch (Exception e) {
            log.error("Failed to trigger user re-indexing", e);
        }

        try {
            restTemplate.postForEntity(postServiceUrl + "/api/v1/posts/internal/reindex", null, Void.class);
            log.info("Triggered post re-indexing.");
        } catch (Exception e) {
            log.error("Failed to trigger post re-indexing", e);
        }
    }
}
