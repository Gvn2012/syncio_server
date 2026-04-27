package io.github.gvn2012.image_uploading_service.grpc;

import io.github.gvn2012.grpc.upload.*;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.SignedUrlRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.SignedUrlResponse;
import io.github.gvn2012.image_uploading_service.services.interfaces.UploadServiceInterface;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class UploadGrpcService extends UploadServiceGrpc.UploadServiceImplBase {

    private final UploadServiceInterface uploadService;

    @Override
    public void getSignedUrls(SignedUrlGrpcRequest request, StreamObserver<SignedUrlGrpcResponse> responseObserver) {
        SignedUrlRequest internalRequest = new SignedUrlRequest(request.getObjectPathsWithContentTypeMap());
        APIResource<SignedUrlResponse> resource = uploadService.getSignedUrls(internalRequest);

        SignedUrlGrpcResponse.Builder responseBuilder = SignedUrlGrpcResponse.newBuilder();
        if (resource.isSuccess() && resource.getData() != null) {
            responseBuilder.putAllSignedUrls(resource.getData().getSignedUrls());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
