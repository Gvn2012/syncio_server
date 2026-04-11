package io.github.gvn2012.notification_service.clients;

import io.github.gvn2012.notification_service.dtos.APIResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.Set;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;

@Component
public class RelationshipClient extends HttpClient {

    public RelationshipClient(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
        super(webClientBuilder, cbFactory, "http://rs");
    }

    public Mono<Set<UUID>> getAudience(UUID userId) {
        return get(
                "/api/v1/rs/relationships/audience/{uid}",
                new ParameterizedTypeReference<APIResource<Set<UUID>>>() {},
                userId.toString())
                .map(res -> {
                    Set<UUID> data = res.getData();
                    return data != null ? data : Set.<UUID>of();
                })
                .onErrorReturn(Set.of());
    }
}
