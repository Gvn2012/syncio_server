package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrgJobLevelRequest {
    @NotBlank(message = "Job level name is required")
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    @NotBlank(message = "Job level code is required")
    @Size(max = 32, message = "Code must not exceed 32 characters")
    private String code;

    @Size(max = 512, message = "Description must not exceed 512 characters")
    private String description;
    
    @Min(value = 0, message = "Rank order must be positive")
    private Integer rankOrder = 0;
}
