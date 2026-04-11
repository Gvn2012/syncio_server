package io.github.gvn2012.notification_service.clients;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class HttpClient {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    protected HttpClient(WebClient.Builder webClientBuilder,
                         ReactiveCircuitBreakerFactory<?, ?> cbFactory,
                         String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.circuitBreaker = cbFactory.create(baseUrl);
    }

    protected <T> Mono<T> get(String uri, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        return webClient.get()
                .uri(uri, uriVariables)
                .retrieve()
                .bodyToMono(responseType)
                .transform(circuitBreaker::run);
    }
}
