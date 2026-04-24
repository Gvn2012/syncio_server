package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostCommentReactionRepository extends JpaRepository<PostCommentReaction, UUID> {
    void deleteByCommentIdAndUserId(UUID commentId, UUID userId);
    boolean existsByCommentIdAndUserId(UUID commentId, UUID userId);
    java.util.Optional<PostCommentReaction> findByCommentIdAndUserId(UUID commentId, UUID userId);
}
