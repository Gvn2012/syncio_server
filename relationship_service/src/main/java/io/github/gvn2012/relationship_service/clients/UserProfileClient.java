package io.github.gvn2012.relationship_service.clients;

import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UserProfileClient extends HttpClient {

    @Value("${syncio.gateway.host:http://localhost:8080}")
    private String gatewayHost;

    public UserProfileClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://user-service");
    }

    public Map<UUID, UserProfileSummary> getUserProfiles(Iterable<UUID> userIds) {
        return getUserProfilesBatch(userIds instanceof java.util.Collection ? (java.util.Collection<UUID>) userIds
                : java.util.stream.StreamSupport.stream(userIds.spliterator(), false).toList());
    }

    public Map<UUID, UserProfileSummary> getUserProfilesBatch(java.util.Collection<UUID> userIds) {
        if (userIds == null || !userIds.iterator().hasNext()) {
            return new ConcurrentHashMap<>();
        }

        log.info("Fetching profiles for {} users in batch from user-service using WebClient", userIds.size());

        try {
            Map<String, Object> body = post(
                    "/api/v1/users/batch",
                    userIds,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            if (body == null || !Boolean.TRUE.equals(body.get("success")) || body.get("data") == null) {
                log.warn("Batch profile retrieval returned unsuccessful response or missing data. Response: {}", body);
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
        } catch (Exception e) {
            log.warn("Failed to fetch user profiles batch via WebClient: {}", e.getMessage());
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    public UserProfileSummary getUserProfile(UUID userId) {
        if (userId == null)
            return null;

        log.info("Fetching profile for user {} from user-service using WebClient", userId);

        try {
            Map<String, Object> body = get(
                    "/api/v1/users/{uid}",
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    },
                    userId.toString())
                    .block();

            if (body == null || !Boolean.TRUE.equals(body.get("success")) || body.get("data") == null) {
                log.warn("Single profile retrieval unsuccessful for user {}. Response: {}", userId, body);
                return fallback(userId);
            }

            return extractSummary(userId, asMap(body.get("data")));
        } catch (Exception e) {
            log.warn("Failed to fetch user profile for {} via WebClient: {}", userId, e.getMessage());
            return fallback(userId);
        }
    }

    private UserProfileSummary extractSummary(UUID userId, Map<String, Object> data) {
        Map<String, Object> userResponse = asMap(getAny(data, "userResponse", "user_response"));
        Map<String, Object> profileResponse = asMap(getAny(data, "userProfileResponse", "user_profile_response"));

        String username = asString(getAny(userResponse, "username"));
        String firstName = asString(getAny(userResponse, "firstName", "first_name"));
        String middleName = asString(getAny(userResponse, "middleName", "middle_name"));
        String lastName = asString(getAny(userResponse, "lastName", "last_name"));

        String displayName = String.join(" ",
                filterBlank(firstName), filterBlank(middleName), filterBlank(lastName)).trim();

        if (!StringUtils.hasText(displayName)) {
            displayName = username != null ? username : "Unknown User";
        }

        // Resolve profile picture from userProfilePictureResponseList
        String profilePictureUrl = null;
        Object picturesObj = getAny(profileResponse, "userProfilePictureResponseList",
                "user_profile_picture_response_list");
        if (picturesObj instanceof java.util.Collection<?> pictures) {
            for (Object picObj : pictures) {
                Map<String, Object> picMap = asMap(picObj);
                Boolean isPrimary = (Boolean) getAny(picMap, "primary");

                // If it's primary or we don't have a picture yet, try to extract URL/path
                if (Boolean.TRUE.equals(isPrimary) || profilePictureUrl == null) {
                    String url = asString(getAny(picMap, "url"));
                    String objectPath = asString(getAny(picMap, "objectPath", "object_path"));

                    if (StringUtils.hasText(objectPath)) {
                        profilePictureUrl = buildProxyUrl(objectPath);
                    } else if (StringUtils.hasText(url)) {
                        profilePictureUrl = url;
                    }

                    if (Boolean.TRUE.equals(isPrimary)) {
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
        if (value == null)
            return null;
        return String.valueOf(value);
    }

    private Object getAny(Map<String, Object> map, String... keys) {
        if (map == null)
            return null;
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null)
                return val;
        }
        return null;
    }

    private String filterBlank(String value) {
        return StringUtils.hasText(value) ? value : "";
    }
}
