package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.CallLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CallLogRepository extends MongoRepository<CallLog, String> {
    List<CallLog> findByConversationIdOrderByStartedAtDesc(String conversationId);
}
