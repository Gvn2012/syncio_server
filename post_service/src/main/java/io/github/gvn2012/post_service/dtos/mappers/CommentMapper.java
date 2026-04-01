package io.github.gvn2012.post_service.dtos.mappers;

import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import io.github.gvn2012.post_service.entities.PostComment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(PostComment comment) {
        if (comment == null) return null;
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .userId(comment.getUserId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .reactionCount(comment.getReactionCount())
                .replyCount(comment.getReplyCount())
                .moderationStatus(comment.getModerationStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
