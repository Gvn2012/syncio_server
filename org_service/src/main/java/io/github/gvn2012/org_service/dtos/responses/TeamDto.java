package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.TeamStatus;
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
public class TeamDto {

    private UUID id;
    private UUID departmentId;
    private String name;
    private String description;
    private UUID teamLeadId;
    private TeamStatus status;
    private Integer maxCapacity;
    private Instant createdAt;
    private Instant updatedAt;
}
