package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamMemberRoleRequest {

    @NotNull(message = "Team role is required")
    private TeamRole teamRole;
}
