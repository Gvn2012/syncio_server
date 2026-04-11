package io.github.gvn2012.shared.kafka_events;

import java.time.LocalDateTime;
import java.util.UUID;

public class PostActivityEvent {

    public enum ActivityType {
        CREATED, UPDATED, DELETED, COMMENTED, REACTED, SHARED, REPORTED
    }

    private UUID eventId;
    private UUID postId;
    private UUID authorId;
    private UUID actorId;
    private String actorName;
    private ActivityType activityType;
    private String postCategory;
    private String metadata;
    private LocalDateTime timestamp;

    public PostActivityEvent() {
        this.eventId = UUID.randomUUID();
    }

    public PostActivityEvent(UUID postId, UUID authorId, UUID actorId, String actorName, ActivityType activityType, String postCategory, String metadata) {
        this.eventId = UUID.randomUUID();
        this.postId = postId;
        this.authorId = authorId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.activityType = activityType;
        this.postCategory = postCategory;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getPostId() { return postId; }
    public void setPostId(UUID postId) { this.postId = postId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public String getPostCategory() { return postCategory; }
    public void setPostCategory(String postCategory) { this.postCategory = postCategory; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
