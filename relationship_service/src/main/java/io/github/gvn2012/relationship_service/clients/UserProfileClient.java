package io.github.gvn2012.relationship_service.clients;

import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileClient {

    @Value("${syncio.user-service.base-url:http://user-service:8082}")
    private String userServiceBaseUrl;

    @Value("${syncio.gateway.host:http://localhost:8080}")
    private String gatewayHost;

    private final RestTemplate restTemplate = buildRestTemplate();

    public Map<UUID, UserProfileSummary> getUserProfiles(Iterable<UUID> userIds) {
        return getUserProfilesBatch(userIds instanceof java.util.Collection ? (java.util.Collection<UUID>) userIds : java.util.stream.StreamSupport.stream(userIds.spliterator(), false).toList());
    }

    public Map<UUID, UserProfileSummary> getUserProfilesBatch(java.util.Collection<UUID> userIds) {
        if (userIds == null || !userIds.iterator().hasNext()) {
            return new ConcurrentHashMap<>();
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(String.valueOf(userServiceBaseUrl))
                    .path("/api/v1/users/batch")
                    .build()
                    .toUri();

            log.info("Requesting batch profiles for {} IDs from user-service: {}", userIds.size(), uri);
            RequestEntity<java.util.Collection<UUID>> request = new RequestEntity<>(userIds, org.springframework.http.HttpMethod.POST, uri);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    request, new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.get("success")) || body.get("data") == null) {
                log.warn("Batch profile retrieval returned unsuccessful response or missing data. Response Code: {}, Response: {}", response.getStatusCode(), body);
                return new ConcurrentHashMap<>();
            }

            Map<UUID, UserProfileSummary> profiles = new ConcurrentHashMap<>();
            Map<Object, Object> dataMap = asObjectMap(body.get("data"));
            log.info("Successfully retrieved {} profiles in batch from user-service", dataMap.size());
            dataMap.forEach((key, detail) -> {
                try {
                    UUID userId = null;
                    if (key instanceof UUID u) {
                        userId = u;
                    } else if (key instanceof String s) {
                        userId = UUID.fromString(s);
                    }

                    if (userId != null) {
                        profiles.put(userId, extractSummary(userId, asMap(detail)));
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse user profile for key: {}", key, e);
                }
            });

            return profiles;
        } catch (Exception ex) {
            log.warn("Failed to retrieve user profiles in batch", ex);
            return new ConcurrentHashMap<>();
        }
    }

    public UserProfileSummary getUserProfile(UUID userId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(String.valueOf(userServiceBaseUrl))
                    .path("/api/v1/users/{userId}")
                    .build(userId.toString());

            RequestEntity<Void> request = new RequestEntity<>(org.springframework.http.HttpMethod.GET, uri);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    request, new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.get("success")) || body.get("data") == null) {
                return fallback(userId);
            }

            return extractSummary(userId, asMap(body.get("data")));
        } catch (RestClientException ex) {
            log.warn("Failed to resolve user profile for {}", userId, ex);
            return fallback(userId);
        }
    }

    private UserProfileSummary extractSummary(UUID userId, Map<String, Object> data) {
        Map<String, Object> userResponse = asMap(data.get("userResponse"));
        Map<String, Object> profileResponse = asMap(data.get("userProfileResponse"));

        String username = asString(userResponse.get("username"));
        String firstName = asString(userResponse.get("firstName"));
        String middleName = asString(userResponse.get("middleName"));
        String lastName = asString(userResponse.get("lastName"));
        String displayName = String.join(" ",
                filterBlank(firstName), filterBlank(middleName), filterBlank(lastName)).trim();
        if (!StringUtils.hasText(displayName)) {
            displayName = username != null ? username : "Unknown User";
        }

        String profilePictureUrl = null;
        Object pictures = profileResponse.get("userProfilePictureResponseList");
        if (pictures instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Map<String, Object> picture = asMap(item);
                if (Boolean.TRUE.equals(picture.get("primary")) || profilePictureUrl == null) {
                    String objectPath = asString(picture.get("objectPath"));
                    String url = asString(picture.get("url"));
                    profilePictureUrl = StringUtils.hasText(objectPath) ? buildProxyUrl(objectPath) : url;
                    if (Boolean.TRUE.equals(picture.get("primary"))) {
                        break;
                    }
                }
            }
        }

        return UserProfileSummary.builder()
                .userId(userId)
                .username(username)
                .displayName(displayName)
                .profilePictureUrl(profilePictureUrl)
                .build();
    }

    private String buildProxyUrl(String objectPath) {
        return UriComponentsBuilder.fromUriString(gatewayHost)
                .path("/api/v1/upload/view")
                .queryParam("path", objectPath)
                .build()
                .toUriString();
    }

    private UserProfileSummary fallback(UUID userId) {
        return UserProfileSummary.builder()
                .userId(userId)
                .displayName("Unknown User")
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> asObjectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<Object, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private String asString(Object value) {
        return value instanceof String s ? s : null;
    }

    private String filterBlank(String value) {
        return StringUtils.hasText(value) ? value : "";
    }

    private RestTemplate buildRestTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());
        return new RestTemplate(factory);
    }
}
