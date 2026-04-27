package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.grpc.relationship.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RelationshipClient {

    @GrpcClient("relationship-service")
    private RelationshipServiceGrpc.RelationshipServiceBlockingStub relationshipServiceStub;

    public Mono<List<UUID>> getFollowers(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getFollowers(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Mono<List<UUID>> getAudience(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getAudience(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()))
                .onErrorReturn(List.of());
    }

    public Mono<List<UUID>> getFollowing(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getFollowing(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Mono<Boolean> isFollowing(UUID sourceId, UUID targetId) {
        return Mono.fromCallable(() -> relationshipServiceStub.isFollowing(
                CheckRelationshipRequest.newBuilder()
                        .setSourceId(sourceId.toString())
                        .setTargetId(targetId.toString())
                        .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getResult())
                .onErrorReturn(false);
    }

    public Mono<Boolean> isBlocked(UUID sourceId, UUID targetId) {
        return Mono.fromCallable(() -> relationshipServiceStub.isBlocked(
                CheckRelationshipRequest.newBuilder()
                        .setSourceId(sourceId.toString())
                        .setTargetId(targetId.toString())
                        .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getResult())
                .onErrorReturn(false);
    }

    public Mono<List<UUID>> getBlockedList(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getBlockedList(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Mono<List<UUID>> getBlockedByList(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getBlockedByList(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()));
    }

    public Mono<List<UUID>> getFriendIds(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getFriendIds(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream().map(UUID::fromString).collect(Collectors.toList()))
                .onErrorReturn(List.of());
    }
}
