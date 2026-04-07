package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OrgClient extends HttpClient {

    public OrgClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://syncio-org:8084");
    }

    public Mono<Map<String, Object>> createOrganization(Map<String, Object> request) {
        return post(
                "/api/v1/orgs",
                request,
                new ParameterizedTypeReference<APIResource<Map<String, Object>>>() {
                })
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new InternalServerErrorException(
                                response.getError() != null
                                        ? response.getError().getMessage()
                                        : "Org service error"));
                    }
                    return Mono.just(response.getData());
                });
    }
}
