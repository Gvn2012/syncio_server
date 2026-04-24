package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.ContentRanking;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContentRankingRepository extends JpaRepository<ContentRanking, UUID> {

    @Query("SELECT c FROM ContentRanking c JOIN c.post p WHERE p.publishedAt >= :cutoff AND p.orgId IS NULL ORDER BY c.totalScore DESC")
    List<ContentRanking> findTopRankingsSince(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Query("SELECT c FROM ContentRanking c JOIN c.post p " +
           "WHERE p.publishedAt < :cursor " +
           "AND p.postCategory NOT IN :excludedCategories " +
           "AND p.orgId IS NULL " +
           "ORDER BY c.totalScore DESC")
    List<ContentRanking> findGlobalTopRankings(
            @Param("cursor") LocalDateTime cursor,
            @Param("excludedCategories") Collection<PostCategory> excludedCategories,
            Pageable pageable
    );

    @Query("SELECT c FROM ContentRanking c JOIN c.post p WHERE p.authorId IN :authorIds AND p.publishedAt >= :cutoff AND p.orgId IS NULL ORDER BY c.totalScore DESC")
    List<ContentRanking> findTopRankingsByAuthors(
            @Param("authorIds") List<UUID> authorIds,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Query("SELECT c FROM ContentRanking c JOIN c.post p " +
           "WHERE p.authorId IN :authorIds " +
           "AND p.publishedAt < :cursor " +
           "AND p.postCategory NOT IN :excludedCategories " +
           "AND p.orgId IS NULL " +
           "ORDER BY c.totalScore DESC")
    List<ContentRanking> findTopRankingsByAuthorsAndCursor(
            @Param("authorIds") List<UUID> authorIds,
            @Param("cursor") LocalDateTime cursor,
            @Param("excludedCategories") Collection<PostCategory> excludedCategories,
            Pageable pageable
    );
}
