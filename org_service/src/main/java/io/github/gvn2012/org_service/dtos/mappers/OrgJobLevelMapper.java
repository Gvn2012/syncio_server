package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobLevelDto;
import io.github.gvn2012.org_service.entities.OrgJobLevel;

public class OrgJobLevelMapper {

    public static OrgJobLevel toEntity(CreateOrgJobLevelRequest request) {
        OrgJobLevel entity = new OrgJobLevel();
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setRankOrder(request.getRankOrder() != null ? request.getRankOrder() : 0);
        entity.setActive(true);
        return entity;
    }

    public static OrgJobLevelDto toDto(OrgJobLevel entity) {
        return OrgJobLevelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .rankOrder(entity.getRankOrder())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
