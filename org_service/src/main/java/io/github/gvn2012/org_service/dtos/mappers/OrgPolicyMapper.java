package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgPolicyDto;
import io.github.gvn2012.org_service.entities.OrgPolicy;

import java.util.UUID;

public class OrgPolicyMapper {

    public static OrgPolicy toEntity(CreateOrgPolicyRequest request, UUID approvedById) {
        OrgPolicy entity = new OrgPolicy();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        if (request.getVersion() != null && !request.getVersion().isBlank()) {
            entity.setVersion(request.getVersion());
        }
        entity.setCategory(request.getCategory());
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setActive(request.getActive() != null ? request.getActive() : true);
        entity.setApprovedById(approvedById);
        return entity;
    }

    public static OrgPolicyDto toDto(OrgPolicy entity) {
        return OrgPolicyDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .version(entity.getVersion())
                .category(entity.getCategory())
                .effectiveDate(entity.getEffectiveDate())
                .active(entity.getActive())
                .approvedById(entity.getApprovedById())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
