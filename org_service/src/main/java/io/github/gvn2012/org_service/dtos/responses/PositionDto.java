package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PositionDto {
    private UUID id;
    private String title;
    private String code;
    private String description;
    
    private UUID departmentId;
    private String departmentName;

    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String currency;
    private Boolean active;
    private String requirements;
    
    private Instant createdAt;
    private Instant updatedAt;
}
