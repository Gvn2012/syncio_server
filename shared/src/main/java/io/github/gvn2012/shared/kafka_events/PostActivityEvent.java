package io.github.gvn2012.shared.kafka_events;

import java.time.LocalDateTime;
import java.util.UUID;

public class PostActivityEvent {

    public enum ActivityType {
        CREATED, UPDATED, DELETED, COMMENTED, REACTED, SHARED, REPORTED
    }

    private UUID postId;
    private UUID authorId;
    private UUID actorId;
    private ActivityType activityType;
    private String metadata;
    private LocalDateTime timestamp;

    public PostActivityEvent() {}

    public PostActivityEvent(UUID postId, UUID authorId, UUID actorId, ActivityType activityType, String metadata) {
        this.postId = postId;
        this.authorId = authorId;
        this.actorId = actorId;
        this.activityType = activityType;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
