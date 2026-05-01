package io.github.gvn2012.websocket_service.clients;

import io.github.gvn2012.grpc.presence.PresenceInternalServiceGrpc;
import io.github.gvn2012.grpc.presence.PresenceUserIdRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PresenceGrpcClient {

    @GrpcClient("presence_service")
    private PresenceInternalServiceGrpc.PresenceInternalServiceBlockingStub presenceStub;

    public void setOnline(String userId) {
        try {
            presenceStub.setOnline(PresenceUserIdRequest.newBuilder().setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to presence_service.SetOnline failed for user {}", userId, e);
        }
    }

    public void setOffline(String userId) {
        try {
            presenceStub.setOffline(PresenceUserIdRequest.newBuilder().setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to presence_service.SetOffline failed for user {}", userId, e);
        }
    }
}
