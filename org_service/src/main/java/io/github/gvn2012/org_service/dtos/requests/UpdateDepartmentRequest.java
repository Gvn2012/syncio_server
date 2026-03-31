package io.github.gvn2012.org_service.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {

    private UUID parentDepartmentId;

    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 1, max = 64)
    private String code;

    private String description;

    private UUID headOfDepartmentId;

    private BigDecimal budget;

    @Size(max = 64)
    private String costCenterCode;
}
