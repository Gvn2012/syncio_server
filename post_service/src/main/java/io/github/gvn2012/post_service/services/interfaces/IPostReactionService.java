package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.PostReaction;

import java.util.List;
import java.util.UUID;

public interface IPostReactionService {
    void addPostReaction(UUID postId, UUID userId, Short reactionTypeId);
    void removePostReaction(UUID postId, UUID userId);
    void toggleReaction(UUID postId, UUID userId, Short reactionTypeId);
    List<PostReaction> getReactionsByPost(UUID postId);
    boolean hasUserReacted(UUID postId, UUID userId);
    void addCommentReaction(UUID commentId, UUID userId, Short reactionTypeId);
    void removeCommentReaction(UUID commentId, UUID userId);
    void toggleCommentReaction(UUID commentId, UUID userId, Short reactionTypeId);
}
