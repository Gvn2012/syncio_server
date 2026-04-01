package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostAnnouncement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostAnnouncementRepository extends JpaRepository<PostAnnouncement, UUID> {

    @Query("SELECT a FROM PostAnnouncement a WHERE (a.pinnedUntil IS NULL OR a.pinnedUntil > :now) ORDER BY a.priority ASC")
    List<PostAnnouncement> findActiveAnnouncements(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE PostAnnouncement a SET a.isPinned = false WHERE a.isPinned = true AND a.pinnedUntil IS NOT NULL AND a.pinnedUntil < :now")
    void unpinExpired(@Param("now") LocalDateTime now);
}
