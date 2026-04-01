package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostTag;
import io.github.gvn2012.post_service.entities.composite_keys.PostTagId;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
    void deleteByPostId(UUID postId);

    @Query("SELECT t, COUNT(pt.post.id) as useCount FROM PostTag pt JOIN pt.tag t WHERE pt.post.publishedAt > :since GROUP BY t.id ORDER BY useCount DESC")
    List<Object[]> findTopTags(@Param("since") java.time.LocalDateTime since, Pageable pageable);
}
