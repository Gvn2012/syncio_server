package io.github.gvn2012.messaging_service.services.impls;

import io.github.gvn2012.messaging_service.models.CallLog;
import io.github.gvn2012.messaging_service.models.CallSession;
import io.github.gvn2012.messaging_service.models.ParticipantEvent;
import io.github.gvn2012.messaging_service.models.enums.CallStatus;
import io.github.gvn2012.messaging_service.repositories.CallLogRepository;
import io.github.gvn2012.messaging_service.repositories.CallSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallSessionService {

    private final CallSessionRepository callSessionRepository;
    private final CallLogRepository callLogRepository;

    public String createSession(String callId, String conversationId, String initiatorId, String mode) {
        Optional<CallSession> existing = callSessionRepository.findByConversationIdAndStatus(conversationId,
                CallStatus.ACTIVE);
        if (existing.isPresent()) {
            log.info("Returning existing active call session {} for conversation {}", existing.get().getCallId(),
                    conversationId);
            return existing.get().getCallId();
        }

        if (callId == null || callId.isEmpty()) {
            callId = UUID.randomUUID().toString();
        }
        CallSession session = CallSession.builder()
                .callId(callId)
                .conversationId(conversationId)
                .initiatorId(initiatorId)
                .callMode(mode)
                .startedAt(System.currentTimeMillis())
                .status(CallStatus.ACTIVE)
                .build();

        callSessionRepository.save(session);
        log.info("Created new call session {} for conversation {}", callId, conversationId);
        return callId;
    }

    public Optional<CallSession> getActiveSession(String callId) {
        return callSessionRepository.findById(callId)
                .filter(session -> session.getStatus() == CallStatus.ACTIVE);
    }

    public Optional<CallSession> getActiveSessionByConversation(String conversationId) {
        return callSessionRepository.findByConversationIdAndStatus(conversationId, CallStatus.ACTIVE);
    }

    public void joinSession(String callId, String userId) {
        getActiveSession(callId).ifPresent(session -> {
            session.getActiveParticipantIds().add(userId);
            session.getParticipantEvents().add(ParticipantEvent.builder()
                    .userId(userId)
                    .action("JOINED")
                    .timestamp(System.currentTimeMillis())
                    .build());

            callSessionRepository.save(session);
            log.info("User {} joined call session {}", userId, callId);
        });
    }

    public void leaveSession(String callId, String userId) {
        getActiveSession(callId).ifPresent(session -> {
            session.getActiveParticipantIds().remove(userId);
            session.getParticipantEvents().add(ParticipantEvent.builder()
                    .userId(userId)
                    .action("LEFT")
                    .timestamp(System.currentTimeMillis())
                    .build());
            callSessionRepository.save(session);
            log.info("User {} left call session {}", userId, callId);
        });
    }

    public boolean endSessionIfEmpty(String callId) {
        Optional<CallSession> optSession = getActiveSession(callId);
        if (optSession.isPresent()) {
            CallSession session = optSession.get();
            if (session.getActiveParticipantIds().isEmpty()) {
                endSession(callId);
                return true;
            }
        }
        return false;
    }

    public void endSession(String callId) {
        getActiveSession(callId).ifPresent(session -> {
            session.setStatus(CallStatus.ENDED);
            callSessionRepository.deleteById(callId);
            log.info("Ended call session {}", callId);
            persistCallLog(session);
        });
    }

    @Async
    public void persistCallLog(CallSession session) {
        long endedAt = System.currentTimeMillis();
        int duration = (int) ((endedAt - session.getStartedAt()) / 1000);

        CallLog logData = CallLog.builder()
                .callId(session.getCallId())
                .conversationId(session.getConversationId())
                .initiatorId(session.getInitiatorId())
                .callMode(session.getCallMode())
                .isVoiceOnly("VOICE".equals(session.getCallMode()))
                .startedAt(session.getStartedAt())
                .endedAt(endedAt)
                .durationSeconds(duration)
                .participantEvents(session.getParticipantEvents())
                .metadata(Map.of(
                        "participantCount", session.getActiveParticipantIds().size(),
                        "endReason", "ALL_LEFT"))
                .build();

        callLogRepository.save(logData);
        log.info("Persisted call log for callId {}", session.getCallId());
    }
}
