package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgSkillDefinitionDto;
import java.util.List;
import java.util.UUID;

public interface IOrgSkillDefinitionService {
    OrgSkillDefinitionDto createSkillDefinition(UUID orgId, CreateOrgSkillDefinitionRequest request);
    OrgSkillDefinitionDto updateSkillDefinition(UUID orgId, UUID skillDefinitionId, UpdateOrgSkillDefinitionRequest request);
    OrgSkillDefinitionDto getSkillDefinitionById(UUID orgId, UUID skillDefinitionId);
    List<OrgSkillDefinitionDto> getSkillDefinitionsByOrgId(UUID orgId);
    List<OrgSkillDefinitionDto> getSkillDefinitionsByOrgIdAndCategory(UUID orgId, String category);
    void deleteSkillDefinition(UUID orgId, UUID skillDefinitionId);
}
