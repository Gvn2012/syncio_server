package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.post_service.dtos.requests.RankingRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.RankingResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class RankingClient extends HttpClient {

    public RankingClient(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
        super(webClientBuilder, cbFactory, "http://syncio-ranking:8000");
    }

    public Mono<RankingResponseDTO> rankPosts(RankingRequestDTO request) {
        log.debug("Sending {} candidates for ranking to {}", request.getCandidates().size(), baseUrl);

        return cbFactory.create("ranking-service").run(
                post("/rank", request, new ParameterizedTypeReference<RankingResponseDTO>() {}),
                throwable -> {
                    log.error("Ranking service call failed (Circuit Breaker): {}", throwable.getMessage());
                    return Mono.empty();
                }
        ).timeout(Duration.ofMillis(500));
    }
}
