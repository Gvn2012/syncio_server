package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.grpc.user.*;
import io.github.gvn2012.post_service.dtos.UserStatusResponse;
import io.github.gvn2012.post_service.dtos.responses.UserSummaryResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public Mono<Boolean> userExists(UUID userId) {
        if (userId == null) return Mono.just(false);
        return Mono.fromCallable(() -> {
            UserDetailResponse response = userServiceStub.getUserProfile(
                    UserRequest.newBuilder().setUserId(userId.toString()).build()
            );
            return response.hasUserResponse() && StringUtils.hasText(response.getUserResponse().getId());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorReturn(false);
    }

    public Mono<UserStatusResponse> getUserStatus(UUID userId) {
        if (userId == null) return Mono.just(new UserStatusResponse(false, false, false, true));
        return Mono.fromCallable(() -> {
            UserDetailResponse response = userServiceStub.getUserProfile(
                    UserRequest.newBuilder().setUserId(userId.toString()).build()
            );
            if (!response.hasUserResponse()) {
                return new UserStatusResponse(false, false, false, true);
            }
            UserResponse user = response.getUserResponse();
            return UserStatusResponse.builder()
                    .active(user.getActive())
                    .suspended(user.getSuspended())
                    .banned(user.getBanned())
                    .softDeleted(false)
                    .build();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorReturn(new UserStatusResponse(false, false, false, true));
    }

    public Mono<String> getUserName(UUID userId) {
        if (userId == null) return Mono.just("Unknown User");
        return Mono.fromCallable(() -> {
            UserDetailResponse response = userServiceStub.getUserProfile(
                    UserRequest.newBuilder().setUserId(userId.toString()).build()
            );
            if (!response.hasUserResponse()) {
                return "Unknown User";
            }
            UserResponse user = response.getUserResponse();
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            if (!StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)) {
                return StringUtils.hasText(user.getUsername()) ? user.getUsername() : "Unknown User";
            }
            return (StringUtils.hasText(firstName) ? firstName : "") + 
                   (StringUtils.hasText(lastName) ? (StringUtils.hasText(firstName) ? " " : "") + lastName : "");
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorReturn("Unknown User");
    }

    public Mono<Map<UUID, UserSummaryResponse>> getUsersSummaries(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Mono.just(new HashMap<>());
        }
        return Mono.fromCallable(() -> {
            java.util.List<String> idStrings = userIds.stream().map(UUID::toString).collect(Collectors.toList());
            UserSummaryBatchResponse response = userServiceStub.getUsersSummary(
                    UserBatchRequest.newBuilder().addAllUserIds(idStrings).build()
            );
            
            Map<UUID, UserSummaryResponse> summaries = new HashMap<>();
            response.getSummariesMap().forEach((id, summary) -> {
                summaries.put(UUID.fromString(id), UserSummaryResponse.builder()
                        .userId(UUID.fromString(summary.getUserId()))
                        .username(summary.getUsername())
                        .displayName(summary.getDisplayName())
                        .avatarUrl(summary.getAvatarUrl())
                        .avatarPath(summary.getAvatarPath())
                        .active(summary.getActive())
                        .suspended(summary.getSuspended())
                        .banned(summary.getBanned())
                        .build());
            });
            return summaries;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorReturn(new HashMap<>());
    }
}
