package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByConversationIdOrderByTimestampDesc(String conversationId, Pageable pageable);
}
