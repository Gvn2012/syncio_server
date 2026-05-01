package io.github.gvn2012.call_service.grpc;

import io.github.gvn2012.call_service.models.CallSession;
import io.github.gvn2012.call_service.services.CallSessionService;
import io.github.gvn2012.grpc.call.BoolCallResponse;
import io.github.gvn2012.grpc.call.CallConvIdRequest;
import io.github.gvn2012.grpc.call.CallInternalServiceGrpc;
import io.github.gvn2012.grpc.call.CreateSessionRequest;
import io.github.gvn2012.grpc.call.EmptyCallResponse;
import io.github.gvn2012.grpc.call.SessionIdRequest;
import io.github.gvn2012.grpc.call.SessionResponse;
import io.github.gvn2012.grpc.call.SessionUserRequest;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class CallInternalServiceImpl extends CallInternalServiceGrpc.CallInternalServiceImplBase {

    private final CallSessionService callSessionService;

    @Override
    public void createSession(CreateSessionRequest request, StreamObserver<SessionResponse> responseObserver) {
        String callId = callSessionService.createSession(
                request.getCallId(), request.getConversationId(),
                request.getInitiatorId(), request.getMode());

        Optional<CallSession> session = callSessionService.getActiveSession(callId);
        responseObserver.onNext(toSessionResponse(session, true));
        responseObserver.onCompleted();
    }

    @Override
    public void joinSession(SessionUserRequest request, StreamObserver<EmptyCallResponse> responseObserver) {
        callSessionService.joinSession(request.getCallId(), request.getUserId());
        responseObserver.onNext(EmptyCallResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void leaveSession(SessionUserRequest request, StreamObserver<EmptyCallResponse> responseObserver) {
        callSessionService.leaveSession(request.getCallId(), request.getUserId());
        responseObserver.onNext(EmptyCallResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void endSessionIfEmpty(SessionIdRequest request, StreamObserver<BoolCallResponse> responseObserver) {
        boolean ended = callSessionService.endSessionIfEmpty(request.getCallId());
        responseObserver.onNext(BoolCallResponse.newBuilder().setResult(ended).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveSession(SessionIdRequest request, StreamObserver<SessionResponse> responseObserver) {
        Optional<CallSession> session = callSessionService.getActiveSession(request.getCallId());
        responseObserver.onNext(toSessionResponse(session, session.isPresent()));
        responseObserver.onCompleted();
    }

    @Override
    public void getActiveSessionByConversation(CallConvIdRequest request,
            StreamObserver<SessionResponse> responseObserver) {
        Optional<CallSession> session = callSessionService
                .getActiveSessionByConversation(request.getConversationId());
        responseObserver.onNext(toSessionResponse(session, session.isPresent()));
        responseObserver.onCompleted();
    }

    private SessionResponse toSessionResponse(Optional<CallSession> optSession, boolean found) {
        SessionResponse.Builder builder = SessionResponse.newBuilder().setFound(found);
        optSession.ifPresent(session -> builder
                .setCallId(session.getCallId())
                .setConversationId(session.getConversationId())
                .setInitiatorId(session.getInitiatorId())
                .setCallMode(session.getCallMode())
                .setStartedAt(session.getStartedAt())
                .setStatus(session.getStatus().name())
                .addAllActiveParticipantIds(session.getActiveParticipantIds()));
        return builder.build();
    }
}
