package io.github.gvn2012.user_service.clients;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.exceptions.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class UploadClient extends HttpClient {

    public UploadClient(WebClient.Builder webClientBuilder) {
        super(webClientBuilder, "http://syncio-uploading:8090");
    }

    public Mono<Map<String, String>> getSignedUrls(Set<String> objectPaths) {
        Map<String, Object> request = Map.of("objectPaths", objectPaths);

        return post(
                "/api/v1/upload/internal/signed-urls",
                request,
                new ParameterizedTypeReference<APIResource<SignedUrlData>>() {
                })
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new InternalServerErrorException(
                                response.getError() != null
                                        ? response.getError().getMessage()
                                        : "Upload service error"));
                    }
                    return Mono.just(response.getData().getSignedUrls());
                })
                .onErrorResume(e -> {
                    log.warn("Failed to resolve signed URLs, falling back to empty map", e);
                    return Mono.just(Map.of());
                });
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class SignedUrlData {
        private Map<String, String> signedUrls;
    }
}
