package io.github.gvn2012.relationship_service.entities.enums;

public enum BlockScope {
    FULL,            // complete block - no interaction possible
    MESSAGES_ONLY,   // can see content but cannot message
    COMMENTS_ONLY,   // can see posts but cannot comment
    MENTIONS_ONLY    // only blocks mentions/tags
}
