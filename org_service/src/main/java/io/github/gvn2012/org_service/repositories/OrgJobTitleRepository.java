package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgJobTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgJobTitleRepository extends JpaRepository<OrgJobTitle, UUID> {

    List<OrgJobTitle> findByOrganization_IdAndActiveTrue(UUID organizationId);

    List<OrgJobTitle> findByOrganization_IdAndDepartment_IdAndActiveTrue(UUID organizationId, UUID departmentId);

    Optional<OrgJobTitle> findByOrganization_IdAndCode(UUID organizationId, String code);

    Optional<OrgJobTitle> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
