package io.github.gvn2012.messaging_service.repositories;

import io.github.gvn2012.messaging_service.models.MediaItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaItemRepository extends MongoRepository<MediaItem, String> {
    List<MediaItem> findByBatchId(String batchId);
    List<MediaItem> findByConversationId(String conversationId);
}
