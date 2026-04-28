package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    Page<Message> findByConversationIdOrderByTimestampDesc(String conversationId, Pageable pageable);

    Optional<Message> findByConversationIdAndBatchId(String conversationId, String batchId);

    List<Message> findByConversationIdAndTimestampAfterOrderByTimestampDesc(String conversationId,
            LocalDateTime timestamp);

    Page<Message> findByConversationIdAndTimestampAfterOrderByTimestampDesc(String conversationId,
            LocalDateTime timestamp, Pageable pageable);

    Page<Message> findByConversationIdAndTimestampBeforeOrderByTimestampDesc(String conversationId,
            LocalDateTime timestamp, Pageable pageable);

    @Query(value = "{ 'conversationId': ?0, 'senderId': { $ne: ?1 }, 'status.?1.status': { $ne: 'SEEN' } }", count = true)
    long countUnreadMessages(String conversationId, String userId);

    @Query(value = "{ 'senderId': { $ne: ?0 }, 'status.?0.status': { $ne: 'SEEN' }, 'deletedAtPerUser.?0': { $exists: false } }", count = true)
    long countTotalUnreadMessages(String userId);

    @Query("{ 'conversationId': ?0, 'senderId': { $ne: ?1 }, 'status.?1.status': { $ne: 'SEEN' } }")
    List<Message> findUnreadMessages(String conversationId, String userId);
}
