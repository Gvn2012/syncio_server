package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OrgSkillDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrgSkillDefinitionRepository extends JpaRepository<OrgSkillDefinition, UUID> {

    List<OrgSkillDefinition> findByOrganization_IdAndActiveTrue(UUID organizationId);

    List<OrgSkillDefinition> findByOrganization_IdAndCategoryAndActiveTrue(UUID organizationId, String category);

    Optional<OrgSkillDefinition> findByOrganization_IdAndCode(UUID organizationId, String code);

    Optional<OrgSkillDefinition> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
