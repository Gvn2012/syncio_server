package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdatePositionRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private UUID departmentId;

    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    @Size(max = 3, message = "Currency must not exceed 3 characters")
    private String currency;

    private Boolean active;

    private String requirements;
}
