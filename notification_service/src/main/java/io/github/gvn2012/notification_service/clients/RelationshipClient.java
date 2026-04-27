package io.github.gvn2012.notification_service.clients;

import io.github.gvn2012.grpc.relationship.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RelationshipClient {

    @GrpcClient("relationship-service")
    private RelationshipServiceGrpc.RelationshipServiceBlockingStub relationshipServiceStub;

    public Mono<Set<UUID>> getAudience(UUID userId) {
        return Mono.fromCallable(() -> relationshipServiceStub.getAudience(
                RelationshipRequest.newBuilder().setUserId(userId.toString()).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> response.getIdsList().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toSet()))
                .onErrorReturn(Set.of());
    }
}
