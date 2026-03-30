package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgAuditLog;
import io.github.gvn2012.org_service.entities.enums.OrgAuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrgAuditLogRepository extends JpaRepository<OrgAuditLog, UUID> {

    Page<OrgAuditLog> findByOrganizationIdOrderByPerformedAtDesc(UUID organizationId, Pageable pageable);

    List<OrgAuditLog> findByOrganizationIdAndAction(UUID organizationId, OrgAuditAction action);

    List<OrgAuditLog> findByOrganizationIdAndPerformedAtBetween(UUID organizationId, Instant from, Instant to);

    List<OrgAuditLog> findByActorUserId(UUID actorUserId);
}
