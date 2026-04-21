package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.ModerationQueue;
import io.github.gvn2012.post_service.entities.enums.ModerationQueueStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModerationQueueRepository extends JpaRepository<ModerationQueue, UUID> {

    List<ModerationQueue> findByStatus(ModerationQueueStatus status, Pageable pageable);

    List<ModerationQueue> findByAssignedTo(UUID moderatorId, Pageable pageable);

    long countByAuthorIdAndCreatedAtAfter(UUID authorId, LocalDateTime threshold);
}
