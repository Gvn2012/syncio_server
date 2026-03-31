package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.InvitationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrgInvitationDto {
    private UUID id;
    private String invitedEmail;
    private UUID invitedByUserId;
    
    private UUID departmentId;
    private UUID positionId;

    private InvitationStatus status;
    private Instant expiresAt;
    private Instant acceptedAt;
    private UUID acceptedByUserId;
    
    private Instant createdAt;
    private Instant updatedAt;
}
