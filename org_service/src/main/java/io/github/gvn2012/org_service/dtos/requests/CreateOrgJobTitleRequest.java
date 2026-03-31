package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrgJobTitleRequest {
    @NotBlank(message = "Job title name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Job title code is required")
    @Size(max = 64, message = "Code must not exceed 64 characters")
    private String code;

    private String description;
    
    // Optional department filtering
    private UUID departmentId;

    private Integer displayOrder = 0;
}
