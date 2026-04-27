package io.github.gvn2012.org_service.clients;

import io.github.gvn2012.grpc.upload.DownloadUrlGrpcRequest;
import io.github.gvn2012.grpc.upload.DownloadUrlGrpcResponse;
import io.github.gvn2012.grpc.upload.UploadServiceGrpc;
import io.github.gvn2012.org_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.org_service.dtos.responses.DownloadUrlResponseDTO;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class UploadClient {

    @GrpcClient("upload-service")
    private UploadServiceGrpc.UploadServiceBlockingStub uploadServiceStub;

    public DownloadUrlResponseDTO getDownloadUrls(DownloadUrlRequestDTO request) {
        try {
            DownloadUrlGrpcRequest grpcRequest = DownloadUrlGrpcRequest.newBuilder()
                    .addAllObjectPaths(request.getObjectPaths())
                    .build();

            DownloadUrlGrpcResponse grpcResponse = uploadServiceStub.getDownloadUrls(grpcRequest);

            return new DownloadUrlResponseDTO(grpcResponse.getDownloadUrlsMap());
        } catch (Exception e) {
            log.error("Failed to fetch download URLs from uploading-service: {}", e.getMessage());
            return new DownloadUrlResponseDTO(Map.of());
        }
    }
}
