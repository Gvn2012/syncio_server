package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient extends HttpClient {

    public AuthClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://syncio-auth:8081");
    }

    public Mono<GenerateLoginTokenResponse> generateToken(GenerateLoginTokenRequest request) {
        return post(
                "/api/v1/auth/generate-tokens",
                request,
                new ParameterizedTypeReference<APIResource<GenerateLoginTokenResponse>>() {
                })
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new InternalServerErrorException(
                                response.getError() != null
                                        ? response.getError().getMessage()
                                        : "Auth service error"));
                    }
                    return Mono.just(response.getData());
                });
    }

}