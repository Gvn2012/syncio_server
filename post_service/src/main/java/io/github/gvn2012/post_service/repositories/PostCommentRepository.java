package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostComment;
import io.github.gvn2012.post_service.entities.enums.CommentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId AND c.status = :status AND c.parentComment IS NULL " +
           "ORDER BY c.isPinned DESC, c.popularity DESC, c.createdAt DESC")
    List<PostComment> findRootComments(@Param("postId") UUID postId, @Param("status") CommentStatus status, Pageable pageable);

    List<PostComment> findByParentCommentIdAndStatusOrderByCreatedAtDesc(UUID parentCommentId, CommentStatus status, Pageable pageable);

    long countByPostIdAndStatus(UUID postId, CommentStatus status);

    @Modifying
    @Query("UPDATE PostComment c SET c.status = :status WHERE c.id = :id OR c.parentComment.id = :id")
    void updateStatusRecursively(@Param("id") UUID id, @Param("status") CommentStatus status);

    @Modifying
    @Query("UPDATE PostComment c SET c.replyCount = c.replyCount + :increment WHERE c.id = :id")
    void incrementReplyCount(@Param("id") UUID id, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostComment c SET c.reactionCount = c.reactionCount + :increment WHERE c.id = :id")
    void incrementReactionCount(@Param("id") UUID id, @Param("increment") int increment);
}
