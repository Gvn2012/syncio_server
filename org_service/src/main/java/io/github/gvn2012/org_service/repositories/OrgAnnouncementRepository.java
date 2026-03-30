package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrgAnnouncementRepository extends JpaRepository<OrgAnnouncement, UUID> {

    Page<OrgAnnouncement> findByOrganization_IdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    Page<OrgAnnouncement> findByOrganization_IdAndDepartmentIsNullOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    Page<OrgAnnouncement> findByOrganization_IdAndDepartment_IdOrderByCreatedAtDesc(UUID organizationId, UUID departmentId, Pageable pageable);

    Page<OrgAnnouncement> findByOrganization_IdAndPinnedTrueOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    Optional<OrgAnnouncement> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
