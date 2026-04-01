package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByAuthorId(UUID authorId, Pageable pageable);

    List<Post> findByStatus(PostStatus status, Pageable pageable);

    List<Post> findByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

    List<Post> findByAuthorIdInAndStatusOrderByPublishedAtDesc(List<UUID> authorIds, PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.status = 'PUBLISHED'")
    List<Post> searchByContentContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.announcement a WHERE a.isPinned = true AND p.status = 'PUBLISHED' ORDER BY a.priority ASC")
    List<Post> findPinnedAndPublishedAnnouncements(Pageable pageable);
}