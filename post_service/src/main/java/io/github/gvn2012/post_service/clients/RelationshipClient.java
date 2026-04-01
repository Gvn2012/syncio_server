package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.exceptions.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class RelationshipClient extends HttpClient {

    public RelationshipClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://relationship-service");
    }

    public Mono<List<UUID>> getFollowers(UUID userId) {
        return get(
                "/api/v1/relationships/followers/{userId}",
                new ParameterizedTypeReference<APIResource<List<UUID>>>() {
                },
                userId.toString())
                .flatMap(this::handleListResponse);
    }

    public Mono<List<UUID>> getFollowing(UUID userId) {
        return get(
                "/api/v1/relationships/following/{userId}",
                new ParameterizedTypeReference<APIResource<List<UUID>>>() {
                },
                userId.toString())
                .flatMap(this::handleListResponse);
    }

    public Mono<Boolean> isFollowing(UUID sourceId, UUID targetId) {
        return get(
                "/api/v1/relationships/{sourceId}/following/{targetId}",
                new ParameterizedTypeReference<APIResource<Boolean>>() {
                },
                sourceId.toString(), targetId.toString())
                .map(response -> response.isSuccess() && Boolean.TRUE.equals(response.getData()))
                .onErrorReturn(false);
    }

    public Mono<Boolean> isBlocked(UUID sourceId, UUID targetId) {
        return get(
                "/api/v1/relationships/{sourceId}/blocked/{targetId}",
                new ParameterizedTypeReference<APIResource<Boolean>>() {
                },
                sourceId.toString(), targetId.toString())
                .map(response -> response.isSuccess() && Boolean.TRUE.equals(response.getData()))
                .onErrorReturn(false);
    }

    private <T> Mono<T> handleListResponse(APIResource<T> response) {
        if (!response.isSuccess() || response.getData() == null) {
            String errorMsg = response.getError() != null ? response.getError().getMessage()
                    : "Relationship service error";
            return Mono.error(new InternalServerErrorException(errorMsg));
        }
        return Mono.just(response.getData());
    }
}
