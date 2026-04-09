package io.github.gvn2012.user_service.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class HttpClient {

    protected final WebClient.Builder webClientBuilder;
    protected final String baseUrl;

    protected <T> Mono<T> get(String uri, Class<T> responseType) {
        return buildClient().get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    protected <T> Mono<T> get(
            String uri,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        return buildClient().get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    protected <T, R> Mono<R> post(
            String uri,
            T body,
            ParameterizedTypeReference<R> responseType
    ) {
        return buildClient().post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    protected <T, R> Mono<R> post(
            String uri,
            T body,
            ParameterizedTypeReference<R> responseType,
            Object... uriVariables
    ) {
        return buildClient().post()
                .uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    protected <R> Mono<R> post(
            String uri,
            ParameterizedTypeReference<R> responseType,
            Object... uriVariables
    ) {
        return buildClient().post()
                .uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }


    protected WebClient buildClient() {
        return webClientBuilder.baseUrl(baseUrl).build();
    }

    protected void addHeaders(WebClient.RequestHeadersSpec<?> request) {}
}