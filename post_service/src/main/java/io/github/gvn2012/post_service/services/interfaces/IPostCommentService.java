package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.responses.CommentPagedResponse;
import io.github.gvn2012.post_service.dtos.responses.CommentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface IPostCommentService {
    CommentResponse addComment(@NonNull UUID postId, UUID authorId, String content, UUID parentCommentId);

    CommentResponse getCommentById(@NonNull UUID postId, @NonNull UUID commentId, UUID viewerId);

    CommentResponse updateComment(@NonNull UUID postId, @NonNull UUID commentId, UUID authorId, String newContent);

    void deleteComment(@NonNull UUID postId, @NonNull UUID commentId, @NonNull UUID authorId);

    CommentPagedResponse getCommentsByPost(@NonNull UUID postId, UUID viewerId, Pageable pageable);

    CommentPagedResponse getReplies(@NonNull UUID postId, @NonNull UUID parentCommentId, UUID viewerId, Pageable pageable);

    void pinComment(@NonNull UUID postId, @NonNull UUID commentId);

    long getCommentCount(@NonNull UUID postId);
}
