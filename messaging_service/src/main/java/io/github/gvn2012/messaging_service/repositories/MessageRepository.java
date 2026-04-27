package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByConversationIdOrderByTimestampDesc(String conversationId, Pageable pageable);
    
    List<Message> findByConversationIdAndTimestampAfterOrderByTimestampDesc(String conversationId, LocalDateTime timestamp);
    
    Page<Message> findByConversationIdAndTimestampAfterOrderByTimestampDesc(String conversationId, LocalDateTime timestamp, Pageable pageable);
}
