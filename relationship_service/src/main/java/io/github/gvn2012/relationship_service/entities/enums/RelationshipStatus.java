package io.github.gvn2012.relationship_service.entities.enums;

public enum RelationshipStatus {
    PENDING,         // friend request sent, awaiting acceptance
    ACTIVE,          // relationship is active
    DECLINED,        // friend request declined
    REMOVED,         // relationship removed by one party
    EXPIRED          // pending request expired
}
