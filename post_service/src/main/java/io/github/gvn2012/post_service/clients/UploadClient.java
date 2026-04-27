package io.github.gvn2012.post_service.clients;

import io.github.gvn2012.grpc.upload.*;
import io.github.gvn2012.post_service.dtos.requests.SignedUrlRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.SignedUrlResponseDTO;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UploadClient {

    @GrpcClient("upload-service")
    private UploadServiceGrpc.UploadServiceBlockingStub uploadServiceStub;

    public SignedUrlResponseDTO getSignedUrls(SignedUrlRequestDTO request) {
        try {
            SignedUrlGrpcRequest grpcRequest = SignedUrlGrpcRequest.newBuilder()
                    .putAllObjectPathsWithContentType(request.getObjectPathsWithContentType())
                    .build();

            SignedUrlGrpcResponse grpcResponse = uploadServiceStub.getSignedUrls(grpcRequest);

            return new SignedUrlResponseDTO(grpcResponse.getSignedUrlsMap());
        } catch (Exception e) {
            return new SignedUrlResponseDTO(Map.of());
        }
    }
}
