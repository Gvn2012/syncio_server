package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobTitleDto;
import io.github.gvn2012.org_service.entities.OrgJobTitle;

public class OrgJobTitleMapper {

    public static OrgJobTitle toEntity(CreateOrgJobTitleRequest request) {
        OrgJobTitle entity = new OrgJobTitle();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        entity.setActive(true);
        return entity;
    }

    public static OrgJobTitleDto toDto(OrgJobTitle entity) {
        return OrgJobTitleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .displayOrder(entity.getDisplayOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
