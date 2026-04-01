package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostAnnouncement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostAnnouncementRepository extends JpaRepository<PostAnnouncement, UUID> {

    @Query("SELECT a FROM PostAnnouncement a WHERE (a.expiresAt IS NULL OR a.expiresAt > :now) ORDER BY a.priority ASC")
    List<PostAnnouncement> findActiveAnnouncements(@Param("now") LocalDateTime now, Pageable pageable);
}
