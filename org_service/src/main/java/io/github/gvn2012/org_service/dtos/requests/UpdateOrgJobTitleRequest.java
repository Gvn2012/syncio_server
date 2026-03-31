package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateOrgJobTitleRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;
    
    private UUID departmentId;

    private Integer displayOrder;
    
    private Boolean active;
}
