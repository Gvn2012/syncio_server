package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrgJobLevelDto {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Integer rankOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
