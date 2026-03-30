package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserAuditLog;
import io.github.gvn2012.user_service.entities.enums.UserAuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, UUID> {

    Page<UserAuditLog> findByUserIdOrderByPerformedAtDesc(UUID userId, Pageable pageable);

    List<UserAuditLog> findByUserIdAndAction(UUID userId, UserAuditAction action);

    List<UserAuditLog> findByUserIdAndPerformedAtBetween(UUID userId, Instant from, Instant to);
}
