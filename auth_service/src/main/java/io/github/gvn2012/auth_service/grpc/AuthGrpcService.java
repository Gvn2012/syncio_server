package io.github.gvn2012.auth_service.grpc;

import io.github.gvn2012.grpc.auth.*;
import io.github.gvn2012.auth_service.dtos.responses.ValidateResponse;
import io.github.gvn2012.auth_service.services.impls.AuthService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void validateToken(TokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        ValidateResponse validation = authService.validateToken(request.getToken());
        
        ValidateTokenResponse.Builder builder = ValidateTokenResponse.newBuilder()
                .setIsValid(validation.getIsValid());
        
        if (validation.getErrorMessage() != null) {
            builder.setErrorMessage(validation.getErrorMessage());
        }
        
        if (validation.getUserId() != null) {
            builder.setUserId(validation.getUserId());
        }
        
        if (validation.getUserRoles() != null) {
            builder.addAllUserRoles(validation.getUserRoles());
        }
        
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void generateToken(GenerateTokenGrpcRequest request, StreamObserver<GenerateTokenGrpcResponse> responseObserver) {
        io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest serviceRequest = 
            new io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest(request.getUsername(), request.getUserId());
        
        io.github.gvn2012.auth_service.dtos.APIResource<io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse> response = 
            authService.generateLoginToken(serviceRequest, request.getIpAddress(), request.getUserAgent());
            
        if (response.isSuccess() && response.getData() != null) {
            responseObserver.onNext(GenerateTokenGrpcResponse.newBuilder()
                    .setAccessToken(response.getData().getAccessToken())
                    .setRefreshToken(response.getData().getRefreshToken())
                    .addAllRoles(response.getData().getUserRoles())
                    .build());
        } else {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(response.getMessage())
                    .asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }
}
