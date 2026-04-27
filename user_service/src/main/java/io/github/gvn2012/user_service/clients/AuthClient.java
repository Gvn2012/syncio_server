package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.grpc.auth.*;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class AuthClient {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    public Mono<GenerateLoginTokenResponse> generateToken(GenerateLoginTokenRequest request) {
        return Mono.fromCallable(() -> authServiceStub.generateToken(
                GenerateTokenGrpcRequest.newBuilder()
                        .setUserId(request.getUserId())
                        .setUsername(request.getUsername())
                        .build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> new GenerateLoginTokenResponse(
                        response.getAccessToken(),
                        response.getRefreshToken(),
                        response.getRolesList()))
                .onErrorMap(e -> new InternalServerErrorException("Auth service error: " + e.getMessage()));
    }

}