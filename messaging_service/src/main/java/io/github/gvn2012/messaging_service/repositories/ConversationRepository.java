package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByParticipantsContaining(String userId);
    
    @Query("{ 'participants': ?0, 'deletedAtPerUser.?0': { $exists: false } }")
    List<Conversation> findActiveConversationsForUser(String userId);
}
