package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface IPostCommentService {
    CommentResponse addComment(@NonNull UUID postId, UUID authorId, String content, UUID parentCommentId);

    CommentResponse getCommentById(@NonNull UUID commentId);

    CommentResponse updateComment(@NonNull UUID commentId, UUID authorId, String newContent);

    void deleteComment(@NonNull UUID commentId, @NonNull UUID authorId);

    List<CommentResponse> getCommentsByPost(@NonNull UUID postId, Pageable pageable);

    List<CommentResponse> getReplies(@NonNull UUID parentCommentId, Pageable pageable);

    void pinComment(@NonNull UUID commentId);

    long getCommentCount(@NonNull UUID postId);
}
