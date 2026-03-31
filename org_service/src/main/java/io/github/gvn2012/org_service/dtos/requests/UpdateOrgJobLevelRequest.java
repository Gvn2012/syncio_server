package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrgJobLevelRequest {
    @Size(max = 128, message = "Name must not exceed 128 characters")
    private String name;

    @Size(max = 512, message = "Description must not exceed 512 characters")
    private String description;
    
    @Min(value = 0, message = "Rank order must be positive")
    private Integer rankOrder;
    
    private Boolean active;
}
