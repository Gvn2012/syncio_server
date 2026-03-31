package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgPolicy;
import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrgPolicyRepository extends JpaRepository<OrgPolicy, UUID> {

    Page<OrgPolicy> findByOrganization_Id(UUID organizationId, Pageable pageable);

    Page<OrgPolicy> findByOrganization_IdAndActiveTrue(UUID organizationId, Pageable pageable);

    Page<OrgPolicy> findByOrganization_IdAndCategory(UUID organizationId, PolicyCategory category, Pageable pageable);

    Optional<OrgPolicy> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
