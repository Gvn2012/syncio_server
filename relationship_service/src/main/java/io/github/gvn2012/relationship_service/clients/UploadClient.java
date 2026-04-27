package io.github.gvn2012.relationship_service.clients;

import io.github.gvn2012.grpc.upload.DownloadUrlGrpcRequest;
import io.github.gvn2012.grpc.upload.DownloadUrlGrpcResponse;
import io.github.gvn2012.grpc.upload.UploadServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class UploadClient {

    @GrpcClient("uploading-service")
    private UploadServiceGrpc.UploadServiceBlockingStub uploadServiceStub;

    public Map<String, String> getDownloadUrls(Set<String> objectPaths) {
        if (objectPaths == null || objectPaths.isEmpty()) {
            return Map.of();
        }

        DownloadUrlGrpcRequest request = DownloadUrlGrpcRequest.newBuilder()
                .addAllObjectPaths(objectPaths)
                .build();

        DownloadUrlGrpcResponse response = uploadServiceStub.getDownloadUrls(request);
        return response.getDownloadUrlsMap();
    }
}
