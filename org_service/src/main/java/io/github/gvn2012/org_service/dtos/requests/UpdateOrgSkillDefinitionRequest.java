package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrgSkillDefinitionRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;
    
    @Size(max = 64, message = "Category must not exceed 64 characters")
    private String category;

    private Integer displayOrder;
    
    private Boolean active;
}
