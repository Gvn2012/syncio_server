package io.github.gvn2012.post_service.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Objects;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;

import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public abstract class HttpClient {

    protected final WebClient.Builder webClientBuilder;
    protected final ReactiveCircuitBreakerFactory<?, ?> cbFactory;

    @NonNull
    protected final String baseUrl;

    protected <T> Mono<T> get(@NonNull String uri, @NonNull Class<T> responseType) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        return buildClient().get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType)
                .transform(it -> rcb.run(it));
    }

    protected <T> Mono<T> get(
            @NonNull String uri,
            @NonNull ParameterizedTypeReference<T> responseType,
            @NonNull Object... uriVariables) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        return buildClient().get()
                .uri(uri, uriVariables)
                .accept(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .retrieve()
                .bodyToMono(responseType)
                .transform(it -> rcb.run(it));
    }

    protected <T, R> Mono<R> post(
            @NonNull String uri,
            @NonNull T body,
            @NonNull ParameterizedTypeReference<R> responseType) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        return buildClient().post()
                .uri(uri)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .transform(it -> rcb.run(it));
    }

    protected WebClient buildClient() {
        return webClientBuilder.baseUrl(baseUrl).build();
    }
}