package io.github.gvn2012.relationship_service.entities.enums;

public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,   // sender cancelled
    EXPIRED,
    BLOCKED      // receiver blocked sender
}
