package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.responses.OrganizationMemberDto;
import io.github.gvn2012.org_service.entities.OrganizationMember;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMemberMapper implements IMapper<OrganizationMember, OrganizationMemberDto> {

    @Override
    public OrganizationMemberDto toDto(OrganizationMember entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationMemberDto.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization() != null ? entity.getOrganization().getId() : null)
                .userId(entity.getUserId())
                .status(entity.getStatus())
                .orgRole(entity.getOrgRole())
                .joinedAt(entity.getJoinedAt())
                .leftAt(entity.getLeftAt())
                .invitedByUserId(entity.getInvitedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public OrganizationMember toEntity(OrganizationMemberDto dto) {
        if (dto == null) {
            return null;
        }

        OrganizationMember entity = new OrganizationMember();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setStatus(dto.getStatus());
        entity.setOrgRole(dto.getOrgRole());
        entity.setJoinedAt(dto.getJoinedAt());
        entity.setLeftAt(dto.getLeftAt());
        entity.setInvitedByUserId(dto.getInvitedByUserId());
        return entity;
    }
}
