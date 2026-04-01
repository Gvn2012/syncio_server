package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.CommentModerationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private UUID postId;
    private UUID userId;
    private UUID parentCommentId;
    private String content;
    private Integer reactionCount;
    private Integer replyCount;
    private CommentModerationStatus moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
