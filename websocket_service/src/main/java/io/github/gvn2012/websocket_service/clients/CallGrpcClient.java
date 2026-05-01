package io.github.gvn2012.websocket_service.clients;

import io.github.gvn2012.grpc.call.CallConvIdRequest;
import io.github.gvn2012.grpc.call.CallInternalServiceGrpc;
import io.github.gvn2012.grpc.call.CreateSessionRequest;
import io.github.gvn2012.grpc.call.SessionIdRequest;
import io.github.gvn2012.grpc.call.SessionResponse;
import io.github.gvn2012.grpc.call.SessionUserRequest;
import io.github.gvn2012.grpc.call.BoolCallResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CallGrpcClient {

    @GrpcClient("call_service")
    private CallInternalServiceGrpc.CallInternalServiceBlockingStub callStub;

    public SessionResponse createSession(String callId, String conversationId, String initiatorId, String mode) {
        try {
            CreateSessionRequest.Builder builder = CreateSessionRequest.newBuilder()
                    .setConversationId(conversationId).setInitiatorId(initiatorId).setMode(mode);
            if (callId != null) builder.setCallId(callId);
            return callStub.createSession(builder.build());
        } catch (Exception e) {
            log.error("gRPC call to call_service.CreateSession failed", e);
            throw new RuntimeException("Failed to create call session", e);
        }
    }

    public void joinSession(String callId, String userId) {
        try {
            callStub.joinSession(SessionUserRequest.newBuilder()
                    .setCallId(callId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to call_service.JoinSession failed", e);
        }
    }

    public void leaveSession(String callId, String userId) {
        try {
            callStub.leaveSession(SessionUserRequest.newBuilder()
                    .setCallId(callId).setUserId(userId).build());
        } catch (Exception e) {
            log.error("gRPC call to call_service.LeaveSession failed", e);
        }
    }

    public boolean endSessionIfEmpty(String callId) {
        try {
            BoolCallResponse response = callStub.endSessionIfEmpty(
                    SessionIdRequest.newBuilder().setCallId(callId).build());
            return response.getResult();
        } catch (Exception e) {
            log.error("gRPC call to call_service.EndSessionIfEmpty failed", e);
            return false;
        }
    }

    public SessionResponse getActiveSession(String callId) {
        try {
            return callStub.getActiveSession(SessionIdRequest.newBuilder().setCallId(callId).build());
        } catch (Exception e) {
            log.error("gRPC call to call_service.GetActiveSession failed", e);
            return SessionResponse.newBuilder().setFound(false).build();
        }
    }

    public SessionResponse getActiveSessionByConversation(String conversationId) {
        try {
            return callStub.getActiveSessionByConversation(
                    CallConvIdRequest.newBuilder().setConversationId(conversationId).build());
        } catch (Exception e) {
            log.error("gRPC call to call_service.GetActiveSessionByConversation failed", e);
            return SessionResponse.newBuilder().setFound(false).build();
        }
    }
}
