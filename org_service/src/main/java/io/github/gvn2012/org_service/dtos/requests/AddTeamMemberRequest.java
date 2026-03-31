package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTeamMemberRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private TeamRole teamRole;
}
