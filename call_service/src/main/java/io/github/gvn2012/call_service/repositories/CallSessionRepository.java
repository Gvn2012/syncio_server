package io.github.gvn2012.call_service.repositories;

import io.github.gvn2012.call_service.models.CallSession;
import io.github.gvn2012.call_service.models.enums.CallStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CallSessionRepository extends CrudRepository<CallSession, String> {
    Optional<CallSession> findByConversationIdAndStatus(String conversationId, CallStatus status);
}
