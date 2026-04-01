package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    List<PostComment> findByParentCommentId(UUID parentCommentId, Pageable pageable);

    long countByPostId(UUID postId);
}
