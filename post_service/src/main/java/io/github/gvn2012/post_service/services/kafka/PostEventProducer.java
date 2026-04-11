package io.github.gvn2012.post_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.PostActivityEvent;
import io.github.gvn2012.shared.kafka_events.PostActivityEvent.ActivityType;
import io.github.gvn2012.shared.kafka_events.PostSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "post-events-v2";
    private static final String SEARCH_TOPIC = "post-search-indexing";

    public void publishPostCreated(UUID postId, UUID authorId, String authorName, String category, List<UUID> mentions, List<UUID> assignees) {
        publish(postId, authorId, authorId, authorName, ActivityType.CREATED, category, null, mentions, assignees);
    }

    public void publishPostUpdated(UUID postId, UUID authorId, UUID editorId) {
        publish(postId, authorId, editorId, null, ActivityType.UPDATED, null, null);
    }

    public void publishPostDeleted(UUID postId, UUID authorId) {
        publish(postId, authorId, authorId, null, ActivityType.DELETED, null, null);
    }

    public void publishPostCommented(UUID postId, UUID authorId, UUID commenterId) {
        publish(postId, authorId, commenterId, null, ActivityType.COMMENTED, null, null);
    }

    public void publishPostReacted(UUID postId, UUID authorId, UUID reactorId) {
        publish(postId, authorId, reactorId, null, ActivityType.REACTED, null, null);
    }

    public void publishPostShared(UUID postId, UUID authorId, UUID sharerId) {
        publish(postId, authorId, sharerId, null, ActivityType.SHARED, null, null);
    }

    public void publishPostReported(UUID postId, UUID authorId, UUID reporterId) {
        publish(postId, authorId, reporterId, null, ActivityType.REPORTED, null, null);
    }

    public void publishPostSearchIndexing(PostSearchEvent event) {
        log.info("Publishing post search indexing event: {} for post: {}", event.getOperationType(), event.getPostId());
        kafkaTemplate.send(SEARCH_TOPIC, event.getPostId().toString(), event);
    }

    @SuppressWarnings("null")
    private void publish(UUID postId, UUID authorId, UUID actorId, String actorName, ActivityType type, String category, String metadata, List<UUID> mentions, List<UUID> assignees) {
        PostActivityEvent event = new PostActivityEvent(postId, authorId, actorId, actorName, type, category, metadata);
        event.setMentions(mentions);
        event.setAssignees(assignees);
        log.info("Publishing post event: {} for post: {}", type, postId);
        kafkaTemplate.send(TOPIC, postId.toString(), event);
    }

    @SuppressWarnings("null")
    private void publish(UUID postId, UUID authorId, UUID actorId, String actorName, ActivityType type, String category, String metadata) {
        publish(postId, authorId, actorId, actorName, type, category, metadata, null, null);
    }
}
