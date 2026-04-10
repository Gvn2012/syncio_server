package io.github.gvn2012.post_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.PostActivityEvent;
import io.github.gvn2012.shared.kafka_events.PostActivityEvent.ActivityType;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "post-events-v2";
    private static final String SEARCH_TOPIC = "post-search-indexing";

    public void publishPostCreated(UUID postId, UUID authorId) {
        publish(postId, authorId, authorId, ActivityType.CREATED, null);
    }

    public void publishPostUpdated(UUID postId, UUID authorId, UUID editorId) {
        publish(postId, authorId, editorId, ActivityType.UPDATED, null);
    }

    public void publishPostDeleted(UUID postId, UUID authorId) {
        publish(postId, authorId, authorId, ActivityType.DELETED, null);
    }

    public void publishPostCommented(UUID postId, UUID authorId, UUID commenterId) {
        publish(postId, authorId, commenterId, ActivityType.COMMENTED, null);
    }

    public void publishPostReacted(UUID postId, UUID authorId, UUID reactorId) {
        publish(postId, authorId, reactorId, ActivityType.REACTED, null);
    }

    public void publishPostShared(UUID postId, UUID authorId, UUID sharerId) {
        publish(postId, authorId, sharerId, ActivityType.SHARED, null);
    }

    public void publishPostReported(UUID postId, UUID authorId, UUID reporterId) {
        publish(postId, authorId, reporterId, ActivityType.REPORTED, null);
    }

    public void publishPostSearchIndexing(PostSearchEvent event) {
        log.info("Publishing post search indexing event: {} for post: {}", event.getOperationType(), event.getPostId());
        kafkaTemplate.send(SEARCH_TOPIC, event.getPostId().toString(), event);
    }

    @SuppressWarnings("null")
    private void publish(UUID postId, UUID authorId, UUID actorId, ActivityType type, String metadata) {
        PostActivityEvent event = new PostActivityEvent(postId, authorId, actorId, type, metadata);
        log.info("Publishing post event: {} for post: {}", type, postId);
        kafkaTemplate.send(TOPIC, postId.toString(), event);
    }
}
