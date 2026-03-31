package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgSkillDefinitionDto;
import io.github.gvn2012.org_service.entities.OrgSkillDefinition;

public class OrgSkillDefinitionMapper {

    public static OrgSkillDefinition toEntity(CreateOrgSkillDefinitionRequest request) {
        OrgSkillDefinition entity = new OrgSkillDefinition();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        entity.setActive(true);
        return entity;
    }

    public static OrgSkillDefinitionDto toDto(OrgSkillDefinition entity) {
        return OrgSkillDefinitionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
