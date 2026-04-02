package io.github.gvn2012.auth_service.clients;

import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.responses.GetUserRoleResponse;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PermissionClient extends HttpClient {

    public PermissionClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://permission-service");
    }

    public Mono<GetUserRoleResponse> getUserRole(String userId) {

        return get(
                "/api/v1/permissions/user/{uid}/role",
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