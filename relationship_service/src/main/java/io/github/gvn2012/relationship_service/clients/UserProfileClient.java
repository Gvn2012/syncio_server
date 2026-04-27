package io.github.gvn2012.relationship_service.clients;

import io.github.gvn2012.grpc.user.*;
import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class UserProfileClient {

    @Value("${syncio.gateway.host:http://syncio.site}")
    private String gatewayHost;

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public Map<UUID, UserProfileSummary> getUserProfiles(Iterable<UUID> userIds) {
        if (userIds == null || !userIds.iterator().hasNext()) {
            return new ConcurrentHashMap<>();
        }

        java.util.List<String> idStrings = StreamSupport.stream(userIds.spliterator(), false)
                .map(UUID::toString)
                .collect(Collectors.toList());

        log.info("Fetching profiles for {} users in batch from user-service using gRPC", idStrings.size());

        try {
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(idStrings).build()
            );

            Map<UUID, UserProfileSummary> profiles = new ConcurrentHashMap<>();
            response.getSummariesMap().forEach((id, summary) -> {
                UUID userId = UUID.fromString(id);
                String avatarUrl = summary.getAvatarUrl();
                if (StringUtils.hasText(summary.getAvatarPath())) {
                    avatarUrl = buildProxyUrl(summary.getAvatarPath());
                }

                profiles.put(userId, UserProfileSummary.builder()
                        .userId(userId)
                        .username(summary.getUsername())
                        .displayName(summary.getDisplayName())
                        .profilePictureUrl(avatarUrl)
                        .build());
            });

            return profiles;
        } catch (Exception e) {
            log.warn("Failed to fetch user profiles batch via gRPC: {}", e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    public UserProfileSummary getUserProfile(UUID userId) {
        if (userId == null) return null;

        log.info("Fetching profile for user {} from user-service using gRPC", userId);

        try {
            UserDetailResponse response = userServiceStub.getUserProfile(
                    UserRequest.newBuilder().setUserId(userId.toString()).build()
            );

            UserResponse user = response.getUserResponse();
            UserProfileResponse profile = response.getUserProfileResponse();

            String displayName = String.join(" ",
                    filterBlank(user.getFirstName()),
                    filterBlank(user.getMiddleName()),
                    filterBlank(user.getLastName())).trim();

            if (!StringUtils.hasText(displayName)) {
                displayName = StringUtils.hasText(user.getUsername()) ? user.getUsername() : "Unknown User";
            }

            String profilePictureUrl = null;
            for (UserProfilePicture pic : profile.getUserProfilePictureListList()) {
                if (pic.getPrimary() || profilePictureUrl == null) {
                    if (StringUtils.hasText(pic.getObjectPath())) {
                        profilePictureUrl = buildProxyUrl(pic.getObjectPath());
                    } else if (StringUtils.hasText(pic.getUrl())) {
                        profilePictureUrl = pic.getUrl();
                    }
                    if (pic.getPrimary()) break;
                }
            }

            return UserProfileSummary.builder()
                    .userId(userId)
                    .username(user.getUsername())
                    .displayName(displayName)
                    .profilePictureUrl(profilePictureUrl)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to fetch user profile for {} via gRPC: {}", userId, e.getMessage());
            return fallback(userId);
        }
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

    private String filterBlank(String value) {
        return StringUtils.hasText(value) ? value : "";
    }
}
