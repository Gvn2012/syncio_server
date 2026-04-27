package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.grpc.ranking.RankingRequest;
import io.github.gvn2012.grpc.ranking.PostFeature;
import io.github.gvn2012.grpc.ranking.RankingResponse;
import io.github.gvn2012.grpc.ranking.RankingServiceGrpc;
import io.github.gvn2012.post_service.dtos.requests.RankingRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.RankingResponseDTO;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RankingClient {

        @GrpcClient("ranking-service")
        private RankingServiceGrpc.RankingServiceBlockingStub rankingServiceStub;

        private final ReactiveCircuitBreakerFactory<?, ?> cbFactory;

        public RankingClient(ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
                this.cbFactory = cbFactory;
        }

        public Mono<RankingResponseDTO> rankPosts(RankingRequestDTO request) {
                log.debug("Sending {} candidates for ranking via gRPC", request.getCandidates().size());

                RankingRequest grpcRequest = RankingRequest.newBuilder()
                                .setUserId(request.getUserId().toString())
                                .addAllCandidates(request.getCandidates().stream()
                                                .map(c -> PostFeature.newBuilder()
                                                                .setPostId(c.getPostId().toString())
                                                                .setAuthorId(c.getAuthorId().toString())
                                                                .setAuthorAffinity(c.getAuthorAffinity() != null
                                                                                ? c.getAuthorAffinity()
                                                                                : 0.0)
                                                                .setVelocityScore(c.getVelocityScore() != null
                                                                                ? c.getVelocityScore()
                                                                                : 0.0)
                                                                .setRecencyHours(c.getRecencyHours() != null
                                                                                ? c.getRecencyHours()
                                                                                : 0.0)
                                                                .setCategory(c.getCategory() != null ? c.getCategory()
                                                                                : "")
                                                                .setMediaCount(c.getMediaCount() != null
                                                                                ? c.getMediaCount()
                                                                                : 0)
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();

                return cbFactory.create("ranking-service").run(
                                Mono.fromCallable(() -> rankingServiceStub.rankPosts(grpcRequest))
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .map(response -> RankingResponseDTO.builder()
                                                                .userId(request.getUserId())
                                                                .rankedCandidates(response.getRankedCandidatesList()
                                                                                .stream()
                                                                                .map(rp -> new RankingResponseDTO.RankedPostDTO(
                                                                                                java.util.UUID.fromString(
                                                                                                                rp.getPostId()),
                                                                                                rp.getScore()))
                                                                                .collect(Collectors.toList()))
                                                                .build()),
                                throwable -> {
                                        log.error("Ranking service gRPC call failed (Circuit Breaker): {}",
                                                        throwable.getMessage());
                                        return Mono.empty();
                                }).timeout(Duration.ofMillis(500));
        }
}
