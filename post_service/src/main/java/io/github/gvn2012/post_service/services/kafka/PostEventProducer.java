package io.github.gvn2012.post_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.PostActivityEvent;
import io.github.gvn2012.shared.kafka_events.PostActivityEvent.ActivityType;
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
    private static final String TOPIC = "post-events";

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

    @SuppressWarnings("null")
    private void publish(UUID postId, UUID authorId, UUID actorId, ActivityType type, String metadata) {
        PostActivityEvent event = new PostActivityEvent(postId, authorId, actorId, type, metadata);
        log.info("Publishing post event: {} for post: {}", type, postId);
        kafkaTemplate.send(TOPIC, postId.toString(), event);
    }
}
