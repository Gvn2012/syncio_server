package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMemberDto {

    private UUID id;
    private UUID organizationId;
    private UUID userId;
    private MembershipStatus status;
    private String orgRole;
    private Instant joinedAt;
    private Instant leftAt;
    private UUID invitedByUserId;
    private Instant createdAt;
    private Instant updatedAt;
}
