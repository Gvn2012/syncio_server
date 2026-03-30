package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgJobLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgJobLevelRepository extends JpaRepository<OrgJobLevel, UUID> {

    List<OrgJobLevel> findByOrganization_IdAndActiveTrueOrderByRankOrderAsc(UUID organizationId);

    Optional<OrgJobLevel> findByOrganization_IdAndCode(UUID organizationId, String code);

    Optional<OrgJobLevel> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
