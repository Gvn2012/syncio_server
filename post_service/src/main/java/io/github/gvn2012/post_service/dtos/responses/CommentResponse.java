package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.CommentModerationStatus;
import io.github.gvn2012.post_service.entities.enums.CommentStatus;
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
    private CommentStatus status;
    private CommentModerationStatus moderationStatus;
    private Boolean isPinned;
    private Boolean isEdited;
    private Integer editCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;

    private UserSummaryResponse authorInfo;
    private String viewerReaction;
}
