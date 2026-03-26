package io.github.gvn2012.relationship_service.entities.enums;

public enum RelationshipType {
    FOLLOW,          // asymmetric: A follows B
    FRIEND,          // symmetric: requires acceptance
    CLOSE_FRIEND,    // elevated friend status
    ACQUAINTANCE,    // weaker connection
    FAMILY,          // family relationship
    COLLEAGUE        // work relationship
}
