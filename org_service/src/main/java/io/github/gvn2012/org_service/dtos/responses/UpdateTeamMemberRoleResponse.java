package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamMemberRoleResponse {
    private UUID id;
    private TeamRole teamRole;
}
