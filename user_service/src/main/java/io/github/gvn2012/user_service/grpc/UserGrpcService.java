package io.github.gvn2012.user_service.grpc;

import io.github.gvn2012.grpc.user.*;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.UserSummaryResponse;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final IUserService userService;

    @Override
    public void getUserProfile(UserRequest request, StreamObserver<UserDetailResponse> responseObserver) {
        APIResource<GetUserDetailResponse> resource = userService.getUserDetail(request.getUserId());
        
        if (resource.isSuccess() && resource.getData() != null) {
            GetUserDetailResponse detail = resource.getData();
            
            UserResponse.Builder userBuilder = UserResponse.newBuilder();
            if (detail.getUserResponse() != null) {
                userBuilder.setId(detail.getUserResponse().getId())
                        .setUsername(detail.getUserResponse().getUsername())
                        .setFirstName(detail.getUserResponse().getFirstName() != null ? detail.getUserResponse().getFirstName() : "")
                        .setLastName(detail.getUserResponse().getLastName() != null ? detail.getUserResponse().getLastName() : "")
                        .setMiddleName(detail.getUserResponse().getMiddleName() != null ? detail.getUserResponse().getMiddleName() : "")
                        .setActive(detail.getUserResponse().getActive() != null ? detail.getUserResponse().getActive() : false)
                        .setSuspended(detail.getUserResponse().getSuspended() != null ? detail.getUserResponse().getSuspended() : false)
                        .setBanned(detail.getUserResponse().getBanned() != null ? detail.getUserResponse().getBanned() : false);
            }

            UserProfileResponse.Builder profileBuilder = UserProfileResponse.newBuilder();
            if (detail.getUserProfileResponse() != null) {
                profileBuilder.setId(detail.getUserProfileResponse().getId())
                        .setBio(detail.getUserProfileResponse().getBio() != null ? detail.getUserProfileResponse().getBio() : "");
                
                if (detail.getUserProfileResponse().getUserProfilePictureResponseList() != null) {
                    detail.getUserProfileResponse().getUserProfilePictureResponseList().forEach(pic -> {
                        profileBuilder.addUserProfilePictureList(UserProfilePicture.newBuilder()
                                .setUrl(pic.getUrl() != null ? pic.getUrl() : "")
                                .setObjectPath(pic.getObjectPath() != null ? pic.getObjectPath() : "")
                                .setPrimary(pic.getPrimary() != null ? pic.getPrimary() : false)
                                .build());
                    });
                }
            }

            responseObserver.onNext(UserDetailResponse.newBuilder()
                    .setUserResponse(userBuilder.build())
                    .setUserProfileResponse(profileBuilder.build())
                    .build());
        } else {
            responseObserver.onNext(UserDetailResponse.getDefaultInstance());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getUsersSummary(UserBatchRequest request, StreamObserver<UserSummaryBatchResponse> responseObserver) {
        Set<UUID> uuids = request.getUserIdsList().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        
        APIResource<Map<UUID, UserSummaryResponse>> resource = userService.getUsersSummary(uuids);
        
        UserSummaryBatchResponse.Builder responseBuilder = UserSummaryBatchResponse.newBuilder();
        
        if (resource.isSuccess() && resource.getData() != null) {
            resource.getData().forEach((uuid, summary) -> {
                responseBuilder.putSummaries(uuid.toString(), UserSummary.newBuilder()
                        .setUserId(summary.getUserId().toString())
                        .setUsername(summary.getUsername() != null ? summary.getUsername() : "")
                        .setDisplayName(summary.getDisplayName() != null ? summary.getDisplayName() : "")
                        .setAvatarUrl(summary.getAvatarUrl() != null ? summary.getAvatarUrl() : "")
                        .setAvatarPath(summary.getAvatarPath() != null ? summary.getAvatarPath() : "")
                        .setActive(summary.getActive() != null ? summary.getActive() : false)
                        .setSuspended(summary.getSuspended() != null ? summary.getSuspended() : false)
                        .setBanned(summary.getBanned() != null ? summary.getBanned() : false)
                        .build());
            });
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
