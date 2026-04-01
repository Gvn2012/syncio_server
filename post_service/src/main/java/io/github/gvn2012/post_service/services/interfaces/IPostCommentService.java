package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostCommentService {
    CommentResponse addComment(UUID postId, UUID authorId, String content, UUID parentCommentId);
    CommentResponse getCommentById(UUID commentId);
    CommentResponse updateComment(UUID commentId, UUID authorId, String newContent);
    void deleteComment(UUID commentId, UUID authorId);
    List<CommentResponse> getCommentsByPost(UUID postId, Pageable pageable);
    List<CommentResponse> getReplies(UUID parentCommentId, Pageable pageable);
    void pinComment(UUID commentId);
    long getCommentCount(UUID postId);
}
