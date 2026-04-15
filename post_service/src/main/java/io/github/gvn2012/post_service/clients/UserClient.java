package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.UserStatusResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;

@Component
public class UserClient extends HttpClient {

    public UserClient(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
        super(webClientBuilder, cbFactory, "http://syncio-user");
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
                new ParameterizedTypeReference<APIResource<Map<String, Object>>>() {
                },
                userId.toString()).map(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return new UserStatusResponse(false, false, false, true);
                    }
                    Map<String, Object> data = response.getData();
                    Map<String, Object> userBody = (Map<String, Object>) data.get("userResponse");
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

    public Mono<String> getUserName(UUID userId) {
        return get(
                "/api/v1/users/{userId}",
                new ParameterizedTypeReference<APIResource<Map<String, Object>>>() {
                },
                userId.toString()).map(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return "Unknown User";
                    }
                    Map<String, Object> data = response.getData();
                    Map<String, Object> userBody = (Map<String, Object>) data.get("userResponse");
                    if (userBody == null) {
                        return "Unknown User";
                    }
                    String firstName = (String) userBody.get("firstName");
                    String lastName = (String) userBody.get("lastName");
                    if (firstName == null && lastName == null)
                        return "Unknown User";
                    return (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
                }).onErrorReturn("Unknown User");
    }
}
