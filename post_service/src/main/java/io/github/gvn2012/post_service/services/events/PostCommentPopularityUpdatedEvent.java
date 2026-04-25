package io.github.gvn2012.post_service.services.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostCommentPopularityUpdatedEvent {
    private final UUID commentId;
}
