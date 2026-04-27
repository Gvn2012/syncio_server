package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.grpc.upload.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class UploadClient {

    @GrpcClient("uploading-service")
    private UploadServiceGrpc.UploadServiceBlockingStub uploadServiceStub;

    public Mono<Map<String, String>> getSignedUrls(Set<String> objectPaths) {
        return Mono.fromCallable(() -> {
            Map<String, String> requestMap = new java.util.HashMap<>();
            objectPaths.forEach(path -> requestMap.put(path, ""));

            return uploadServiceStub.getSignedUrls(
                    SignedUrlGrpcRequest.newBuilder()
                            .putAllObjectPathsWithContentType(requestMap)
                            .build());
        })
                .subscribeOn(Schedulers.boundedElastic())
                .map(SignedUrlGrpcResponse::getSignedUrlsMap)
                .onErrorResume(e -> {
                    log.warn("Failed to resolve signed URLs via gRPC, falling back to empty map", e);
                    return Mono.just(Map.of());
                });
    }

}
