package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.PostComment;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostCommentService {
    PostComment addComment(UUID postId, UUID authorId, String content, UUID parentCommentId);
    PostComment getCommentById(UUID commentId);
    PostComment updateComment(UUID commentId, UUID authorId, String newContent);
    void deleteComment(UUID commentId, UUID authorId);
    List<PostComment> getCommentsByPost(UUID postId, Pageable pageable);
    List<PostComment> getReplies(UUID parentCommentId, Pageable pageable);
    void pinComment(UUID commentId);
    long getCommentCount(UUID postId);
}
