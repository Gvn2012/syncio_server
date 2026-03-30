package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgPolicy;
import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgPolicyRepository extends JpaRepository<OrgPolicy, UUID> {

    List<OrgPolicy> findByOrganization_IdAndActiveTrue(UUID organizationId);

    List<OrgPolicy> findByOrganization_IdAndCategory(UUID organizationId, PolicyCategory category);

    Optional<OrgPolicy> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
