package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.exceptions.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import io.github.gvn2012.post_service.dtos.UserStatusResponse;

@Component
public class UserClient extends HttpClient {

    public UserClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://user-service");
    }

    public Mono<Boolean> userExists(UUID userId) {
        return get(
                "/api/v1/users/{userId}",
                new ParameterizedTypeReference<APIResource<Object>>() {
                },
                userId.toString()).map(response -> response.isSuccess() && response.getData() != null)
                .onErrorReturn(false);
    }

    public Mono<UserStatusResponse> getUserStatus(UUID userId) {
        return get(
                "/api/v1/users/{userId}",
                new ParameterizedTypeReference<APIResource<java.util.Map<String, Object>>>() {
                },
                userId.toString()).map(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return new UserStatusResponse(false, false, false, true);
                    }
                    java.util.Map<String, Object> data = response.getData();
                    java.util.Map<String, Object> userBody = (java.util.Map<String, Object>) data.get("userResponse");
                    if (userBody == null) {
                        return new UserStatusResponse(false, false, false, true);
                    }
                    return UserStatusResponse.builder()
                            .active(Boolean.TRUE.equals(userBody.get("active")))
                            .suspended(Boolean.TRUE.equals(userBody.get("suspended")))
                            .banned(Boolean.TRUE.equals(userBody.get("banned")))
                            .softDeleted(false)
                            .build();
                }).onErrorReturn(new UserStatusResponse(false, false, false, true));
    }
}
