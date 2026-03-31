package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.TeamRole;
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
public class TeamMemberDto {

    private UUID id;
    private UUID teamId;
    private UUID userId;
    private TeamRole teamRole;
    private Instant joinedAt;
    private Instant leftAt;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
