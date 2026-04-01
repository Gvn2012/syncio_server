package io.github.gvn2012.shared.kafka_events;

import java.util.UUID;

public class RelationshipChangedEvent {
    public enum ChangeType {
        FOLLOW, UNFOLLOW, FRIEND_REQUEST_SENT, FRIEND_REQUEST_ACCEPTED, BLOCK, UNBLOCK, MUTE, UNMUTE
    }

    private UUID sourceUserId;
    private UUID targetUserId;
    private ChangeType changeType;

    public RelationshipChangedEvent() {}

    public RelationshipChangedEvent(UUID sourceUserId, UUID targetUserId, ChangeType changeType) {
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.changeType = changeType;
    }

    public UUID getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(UUID sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }
}
