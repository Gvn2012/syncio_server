package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgInvitationRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgInvitationDto;
import io.github.gvn2012.org_service.entities.OrgInvitation;
import io.github.gvn2012.org_service.entities.enums.InvitationStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class OrgInvitationMapper {

    public static OrgInvitation toEntity(CreateOrgInvitationRequest request, UUID invitedByUserId, String tokenHash) {
        OrgInvitation entity = new OrgInvitation();
        entity.setInvitedEmail(request.getInvitedEmail());
        entity.setInvitedByUserId(invitedByUserId);
        entity.setTokenHash(tokenHash);
        entity.setStatus(InvitationStatus.PENDING);
        
        Instant expiresAt = request.getExpiresAt() != null ? request.getExpiresAt() : Instant.now().plus(7, ChronoUnit.DAYS);
        entity.setExpiresAt(expiresAt);
        
        return entity;
    }

    public static OrgInvitationDto toDto(OrgInvitation entity) {
        return OrgInvitationDto.builder()
                .id(entity.getId())
                .invitedEmail(entity.getInvitedEmail())
                .invitedByUserId(entity.getInvitedByUserId())
                .departmentId(entity.getDepartmentId())
                .positionId(entity.getPositionId())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .acceptedAt(entity.getAcceptedAt())
                .acceptedByUserId(entity.getAcceptedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
