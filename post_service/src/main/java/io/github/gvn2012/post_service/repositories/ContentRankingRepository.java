package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.ContentRanking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ContentRankingRepository extends JpaRepository<ContentRanking, UUID> {

    @Query("SELECT c FROM ContentRanking c JOIN c.post p WHERE p.publishedAt >= :cutoff ORDER BY c.totalScore DESC")
    List<ContentRanking> findTopRankingsSince(
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Query("SELECT c FROM ContentRanking c JOIN c.post p WHERE p.authorId IN :authorIds AND p.publishedAt >= :cutoff ORDER BY c.totalScore DESC")
    List<ContentRanking> findTopRankingsByAuthors(
            @Param("authorIds") List<UUID> authorIds,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );
}
