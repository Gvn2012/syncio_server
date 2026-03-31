package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateOrgPolicyRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 32, message = "Version must not exceed 32 characters")
    private String version;

    @NotNull(message = "Category is required")
    private PolicyCategory category;

    private LocalDate effectiveDate;
    
    private Boolean active = true;
}
