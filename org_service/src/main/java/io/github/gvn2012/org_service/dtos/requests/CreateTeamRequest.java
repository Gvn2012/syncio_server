package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 255)
    private String name;

    private String description;

    private UUID teamLeadId;

    @Min(value = 1, message = "Max capacity must be greater than 0")
    private Integer maxCapacity;
}
