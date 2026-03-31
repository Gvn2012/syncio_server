package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateOrgPolicyRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;

    @Size(max = 32, message = "Version must not exceed 32 characters")
    private String version;

    private PolicyCategory category;
    
    private LocalDate effectiveDate;
    
    private Boolean active;
}
