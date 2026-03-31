package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrgJobTitleDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    
    private UUID departmentId;
    private String departmentName;

    private Integer displayOrder;
    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
