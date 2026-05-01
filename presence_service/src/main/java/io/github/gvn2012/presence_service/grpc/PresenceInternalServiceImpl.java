package io.github.gvn2012.presence_service.grpc;

import io.github.gvn2012.grpc.presence.BoolPresenceResponse;
import io.github.gvn2012.grpc.presence.EmptyPresenceResponse;
import io.github.gvn2012.grpc.presence.PresenceInternalServiceGrpc;
import io.github.gvn2012.grpc.presence.PresenceUserIdRequest;
import io.github.gvn2012.grpc.presence.UserIdsRequest;
import io.github.gvn2012.grpc.presence.UserIdsResponse;
import io.github.gvn2012.presence_service.services.PresenceService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class PresenceInternalServiceImpl extends PresenceInternalServiceGrpc.PresenceInternalServiceImplBase {

    private final PresenceService presenceService;

    @Override
    public void setOnline(PresenceUserIdRequest request, StreamObserver<EmptyPresenceResponse> responseObserver) {
        presenceService.setUserOnline(request.getUserId());
        responseObserver.onNext(EmptyPresenceResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void setOffline(PresenceUserIdRequest request, StreamObserver<EmptyPresenceResponse> responseObserver) {
        presenceService.setUserOffline(request.getUserId());
        responseObserver.onNext(EmptyPresenceResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void isOnline(PresenceUserIdRequest request, StreamObserver<BoolPresenceResponse> responseObserver) {
        boolean online = presenceService.isUserOnline(request.getUserId());
        responseObserver.onNext(BoolPresenceResponse.newBuilder().setResult(online).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getOnlineUserIds(UserIdsRequest request, StreamObserver<UserIdsResponse> responseObserver) {
        List<String> onlineIds = presenceService.getOnlineUserIds(request.getUserIdsList());
        responseObserver.onNext(UserIdsResponse.newBuilder().addAllOnlineUserIds(onlineIds).build());
        responseObserver.onCompleted();
    }
}
