package io.github.gvn2012.relationship_service.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@RequiredArgsConstructor
public abstract class HttpClient {

    protected final WebClient.Builder webClientBuilder;
    protected final ReactiveCircuitBreakerFactory<?, ?> cbFactory;
    protected final String baseUrl;

    protected <T> Mono<T> get(@NonNull String uri, @NonNull Class<T> responseType) {
        return get(uri, responseType, null);
    }

    protected <T> Mono<T> get(@NonNull String uri, @NonNull Class<T> responseType, Function<Throwable, Mono<T>> fallback) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        Mono<T> call = buildClient().get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
        
        return fallback != null ? rcb.run(call, fallback) : rcb.run(call);
    }

    protected <T> Mono<T> get(
            @NonNull String uri,
            @NonNull ParameterizedTypeReference<T> responseType,
            @NonNull Object... uriVariables
    ) {
        return getWithFallback(uri, responseType, null, uriVariables);
    }

    protected <T> Mono<T> getWithFallback(
            @NonNull String uri,
            @NonNull ParameterizedTypeReference<T> responseType,
            Function<Throwable, Mono<T>> fallback,
            @NonNull Object... uriVariables
    ) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        Mono<T> call = buildClient().get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);

        return fallback != null ? rcb.run(call, fallback) : rcb.run(call);
    }

    protected <T, R> Mono<R> post(
            @NonNull String uri,
            @NonNull T body,
            @NonNull ParameterizedTypeReference<R> responseType
    ) {
        return post(uri, body, responseType, null);
    }

    protected <T, R> Mono<R> post(
            @NonNull String uri,
            @NonNull T body,
            @NonNull ParameterizedTypeReference<R> responseType,
            Function<Throwable, Mono<R>> fallback
    ) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        Mono<R> call = buildClient().post()
                .uri(uri)
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);

        return fallback != null ? rcb.run(call, fallback) : rcb.run(call);
    }

    protected <T, R> Mono<R> post(
            @NonNull String uri,
            @NonNull T body,
            @NonNull ParameterizedTypeReference<R> responseType,
            @NonNull Object... uriVariables
    ) {
        return postWithFallback(uri, body, responseType, null, uriVariables);
    }

    protected <T, R> Mono<R> postWithFallback(
            @NonNull String uri,
            @NonNull T body,
            @NonNull ParameterizedTypeReference<R> responseType,
            Function<Throwable, Mono<R>> fallback,
            @NonNull Object... uriVariables
    ) {
        ReactiveCircuitBreaker rcb = cbFactory.create("syncio-circuitbreaker");
        Mono<R> call = buildClient().post()
                .uri(uri, uriVariables)
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);

        return fallback != null ? rcb.run(call, fallback) : rcb.run(call);
    }

    protected WebClient buildClient() {
        return webClientBuilder.baseUrl(java.util.Objects.requireNonNull(baseUrl)).build();
    }
}
