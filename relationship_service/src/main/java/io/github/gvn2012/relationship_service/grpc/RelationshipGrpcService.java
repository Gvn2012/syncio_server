package io.github.gvn2012.relationship_service.grpc;

import io.github.gvn2012.grpc.relationship.*;
import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipResponse;
import io.github.gvn2012.relationship_service.services.interfaces.IRelationshipService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class RelationshipGrpcService extends RelationshipServiceGrpc.RelationshipServiceImplBase {

    private final IRelationshipService relationshipService;
    private final io.github.gvn2012.relationship_service.services.interfaces.IUserBlockService userBlockService;

    @Override
    public void getFollowers(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        APIResource<List<UUID>> resource = relationshipService.getFollowersIds(UUID.fromString(request.getUserId()));
        sendIdsResponse(resource, responseObserver);
    }

    @Override
    public void getFollowing(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        APIResource<List<UUID>> resource = relationshipService.getFollowingIds(UUID.fromString(request.getUserId()));
        sendIdsResponse(resource, responseObserver);
    }

    @Override
    public void getAudience(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        APIResource<Set<UUID>> resource = relationshipService.getAudienceIds(UUID.fromString(request.getUserId()));
        IdsResponse.Builder builder = IdsResponse.newBuilder();
        if (resource.isSuccess() && resource.getData() != null) {
            builder.addAllIds(resource.getData().stream().map(UUID::toString).collect(Collectors.toList()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void isFollowing(CheckRelationshipRequest request, StreamObserver<BooleanResponse> responseObserver) {
        APIResource<Boolean> resource = relationshipService.isFollowing(
                UUID.fromString(request.getSourceId()),
                UUID.fromString(request.getTargetId()));
        sendBooleanResponse(resource, responseObserver);
    }

    @Override
    public void isBlocked(CheckRelationshipRequest request, StreamObserver<BooleanResponse> responseObserver) {
        APIResource<Boolean> resource = relationshipService.isBlocked(
                UUID.fromString(request.getSourceId()),
                UUID.fromString(request.getTargetId()));
        sendBooleanResponse(resource, responseObserver);
    }

    @Override
    public void getBlockedList(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        List<UUID> blocks = userBlockService.getBlockedList(UUID.fromString(request.getUserId()));
        IdsResponse.Builder builder = IdsResponse.newBuilder()
                .addAllIds(blocks.stream().map(UUID::toString).collect(Collectors.toList()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBlockedByList(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        List<UUID> blockedBy = userBlockService.getBlockedByList(UUID.fromString(request.getUserId()));
        IdsResponse.Builder builder = IdsResponse.newBuilder()
                .addAllIds(blockedBy.stream().map(UUID::toString).collect(Collectors.toList()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getFriendIds(RelationshipRequest request, StreamObserver<IdsResponse> responseObserver) {
        APIResource<List<RelationshipResponse>> resource = relationshipService.getFriendList(UUID.fromString(request.getUserId()));
        IdsResponse.Builder builder = IdsResponse.newBuilder();
        if (resource.isSuccess() && resource.getData() != null) {
            UUID userId = UUID.fromString(request.getUserId());
            builder.addAllIds(resource.getData().stream()
                    .map(rel -> rel.getTargetUserId().equals(userId) ? rel.getSourceUserId() : rel.getTargetUserId())
                    .map(UUID::toString)
                    .collect(Collectors.toList()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private void sendIdsResponse(APIResource<List<UUID>> resource, StreamObserver<IdsResponse> responseObserver) {
        IdsResponse.Builder builder = IdsResponse.newBuilder();
        if (resource.isSuccess() && resource.getData() != null) {
            builder.addAllIds(resource.getData().stream().map(UUID::toString).collect(Collectors.toList()));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private void sendBooleanResponse(APIResource<Boolean> resource, StreamObserver<BooleanResponse> responseObserver) {
        BooleanResponse response = BooleanResponse.newBuilder()
                .setResult(resource.isSuccess() && Boolean.TRUE.equals(resource.getData()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
