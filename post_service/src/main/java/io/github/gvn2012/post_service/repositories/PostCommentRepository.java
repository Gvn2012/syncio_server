package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostComment;
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

    List<PostComment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    List<PostComment> findByPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    List<PostComment> findByParentCommentId(UUID parentCommentId, Pageable pageable);

    long countByPostId(UUID postId);

    @Modifying
    @Query("UPDATE PostComment c SET c.replyCount = c.replyCount + :increment WHERE c.id = :id")
    void incrementReplyCount(@Param("id") UUID id, @Param("increment") int increment);

    @Modifying
    @Query("UPDATE PostComment c SET c.reactionCount = c.reactionCount + :increment WHERE c.id = :id")
    void incrementReactionCount(@Param("id") UUID id, @Param("increment") int increment);
}
