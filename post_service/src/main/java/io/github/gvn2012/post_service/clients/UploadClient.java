package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.SignedUrlRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.SignedUrlResponseDTO;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class UploadClient extends HttpClient {

    public UploadClient(WebClient.Builder webClientBuilder, ReactiveCircuitBreakerFactory<?, ?> cbFactory) {
        super(webClientBuilder, cbFactory, "http://syncio-uploading:8090");
    }

    public SignedUrlResponseDTO getSignedUrls(SignedUrlRequestDTO request) {
        return post(
                "/api/v1/upload/internal/signed-urls",
                request,
                new ParameterizedTypeReference<APIResource<SignedUrlResponseDTO>>() {
                }).map(response -> response.isSuccess() && response.getData() != null
                        ? response.getData()
                        : new SignedUrlResponseDTO(Map.of()))
                .onErrorReturn(new SignedUrlResponseDTO(Map.of()))
                .block();
    }
}
