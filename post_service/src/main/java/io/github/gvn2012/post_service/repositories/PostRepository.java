package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

        List<Post> findByAuthorId(UUID authorId, Pageable pageable);

        List<Post> findByStatus(PostStatus status, Pageable pageable);

        List<Post> findByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds AND p.status = :status AND p.orgId IS NULL ORDER BY p.publishedAt DESC")
        List<Post> findByAuthorIdInAndStatusOrderByPublishedAtDesc(
                        @Param("authorIds") List<UUID> authorIds,
                        @Param("status") PostStatus status,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds " +
                        "AND p.status = :status " +
                        "AND p.postCategory NOT IN :excludedCategories " +
                        "AND p.publishedAt < :cursor " +
                        "AND p.orgId IS NULL " +
                        "ORDER BY p.publishedAt DESC")
        List<Post> findByAuthorIdInAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                        @Param("authorIds") List<UUID> authorIds,
                        @Param("status") PostStatus status,
                        @Param("excludedCategories") Collection<io.github.gvn2012.post_service.entities.enums.PostCategory> excludedCategories,
                        @Param("cursor") LocalDateTime cursor,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.visibility = :visibility " +
                        "AND p.status = :status " +
                        "AND p.postCategory NOT IN :excludedCategories " +
                        "AND p.publishedAt < :cursor " +
                        "AND p.orgId IS NULL " +
                        "ORDER BY p.publishedAt DESC")
        List<Post> findByVisibilityAndStatusAndPostCategoryNotInAndPublishedAtBeforeOrderByPublishedAtDesc(
                        @Param("visibility") PostVisibility visibility,
                        @Param("status") PostStatus status,
                        @Param("excludedCategories") Collection<io.github.gvn2012.post_service.entities.enums.PostCategory> excludedCategories,
                        @Param("cursor") LocalDateTime cursor,
                        Pageable pageable);

        @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.status = 'PUBLISHED'")
        List<Post> searchByContentContaining(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT p FROM Post p JOIN p.announcement a WHERE a.isPinned = true AND p.status = 'PUBLISHED' ORDER BY a.priority ASC")
        List<Post> findPinnedAndPublishedAnnouncements(Pageable pageable);

        @Query("SELECT p.parentPost.id FROM Post p " +
                        "WHERE p.authorId = :userId " +
                        "AND p.parentPost.id IN :postIds " +
                        "AND p.isShared = true")
        List<UUID> findSharedPostIdsByAuthor(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);

        @Modifying
        @Query("UPDATE Post p SET p.commentCount = p.commentCount + :increment WHERE p.id = :id")
        void incrementCommentCount(@Param("id") UUID id, @Param("increment") int increment);

        @Modifying
        @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + :increment WHERE p.id = :id")
        void incrementReactionCount(@Param("id") UUID id, @Param("increment") int increment);

        @Modifying
        @Query("UPDATE Post p SET p.shareCount = p.shareCount + :increment WHERE p.id = :id")
        void incrementShareCount(@Param("id") UUID id, @Param("increment") int increment);

        @Modifying
        @Query("UPDATE Post p SET p.viewCount = p.viewCount + :increment WHERE p.id = :id")
        void incrementViewCount(@Param("id") UUID id, @Param("increment") long increment);
}