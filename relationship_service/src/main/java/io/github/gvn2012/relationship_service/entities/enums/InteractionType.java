package io.github.gvn2012.relationship_service.entities.enums;

import lombok.Getter;

@Getter
public enum InteractionType {
    VIEW_PROFILE(0.1),
    LIKE_POST(0.5),
    COMMENT_POST(1.0),
    SHARE_POST(1.5),
    REPLY_COMMENT(0.8),
    MENTION(1.2),
    DIRECT_MESSAGE(2.0),
    STORY_VIEW(0.2),
    STORY_REPLY(1.5),
    TAG_IN_POST(1.5),
    CALL(3.0);

    private final double defaultWeight;

    InteractionType(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

}
