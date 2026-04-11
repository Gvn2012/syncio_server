package io.github.gvn2012.notification_service.clients;

import io.github.gvn2012.notification_service.dtos.APIResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;

@Component
public class RelationshipClient extends HttpClient {

    public RelationshipClient(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
        super(webClientBuilder, cbFactory, "http://rs");
    }

    public Mono<Set<UUID>> getAudience(UUID userId) {
        Mono<List<UUID>> followers = get(
                "/api/v1/rs/relationships/followers/{uid}",
                new ParameterizedTypeReference<APIResource<List<UUID>>>() {},
                userId.toString())
                .map(res -> {
                    List<UUID> data = res.getData();
                    return data != null ? data : List.<UUID>of();
                })
                .onErrorReturn(List.of());

        Mono<List<java.util.Map<String, Object>>> friends = get(
                "/api/v1/rs/relationships/friends/{uid}",
                new ParameterizedTypeReference<APIResource<List<java.util.Map<String, Object>>>>() {},
                userId.toString())
                .map(res -> {
                    List<java.util.Map<String, Object>> data = res.getData();
                    return data != null ? data : List.<java.util.Map<String, Object>>of();
                })
                .onErrorReturn(List.of());

        return Mono.zip(followers, friends).map(tuple -> {
            Set<UUID> audience = new HashSet<>(tuple.getT1());
            for (java.util.Map<String, Object> friend : tuple.getT2()) {
                Object targetId = friend.get("targetUserId");
                if (targetId instanceof String) {
                    audience.add(UUID.fromString((String) targetId));
                } else if (targetId instanceof UUID) {
                    audience.add((UUID) targetId);
                }
            }
            return audience;
        });
    }
}
