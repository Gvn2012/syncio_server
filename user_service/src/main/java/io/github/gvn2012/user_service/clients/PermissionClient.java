package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PermissionClient extends HttpClient {

    public PermissionClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://syncio-permission:8088");
    }

    public Mono<GetUserRoleResponse> getUserRole(String userId) {
        return get(
                "/api/v1/permissions/user/{userId}/role",
                new ParameterizedTypeReference<APIResource<GetUserRoleResponse>>() {
                },
                userId)
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new InternalServerErrorException(
                                response.getError() != null
                                        ? response.getError().getMessage()
                                        : "Permission service error"));
                    }
                    return Mono.just(response.getData());
                });
    }

}