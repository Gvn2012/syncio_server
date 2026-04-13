package io.github.gvn2012.relationship_service.clients;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
        Map<UUID, UserProfileSummary> profiles = new ConcurrentHashMap<>();
        for (UUID userId : userIds) {
            profiles.put(userId, getUserProfile(userId));
        }
        return profiles;
    }

    public UserProfileSummary getUserProfile(UUID userId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(userServiceBaseUrl)
                    .path("/api/v1/users/{userId}")
                    .build(userId.toString());

            RequestEntity<Void> request = new RequestEntity<>(HttpMethod.GET, uri);
            ResponseEntity<APIResource<Map<String, Object>>> response = restTemplate.exchange(
                    request, new ParameterizedTypeReference<>() {});

            APIResource<Map<String, Object>> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                return fallback(userId);
            }

            return extractSummary(userId, body.getData());
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
        return UriComponentsBuilder.fromHttpUrl(gatewayHost)
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
