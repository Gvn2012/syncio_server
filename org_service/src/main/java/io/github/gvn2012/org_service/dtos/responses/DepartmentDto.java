package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.DepartmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    private UUID id;
    private UUID organizationId;
    private UUID parentDepartmentId;
    private String name;
    private String code;
    private String description;
    private UUID headOfDepartmentId;
    private DepartmentStatus status;
    private BigDecimal budget;
    private String costCenterCode;
    private Instant createdAt;
    private Instant updatedAt;
}
