package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.PostEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostEventRepository extends JpaRepository<PostEvent, UUID> {

    @Query("SELECT e FROM PostEvent e WHERE e.startTime > :now AND e.status = 'SCHEDULED' ORDER BY e.startTime ASC")
    List<PostEvent> findUpcomingEvents(LocalDateTime now, Pageable pageable);
}
