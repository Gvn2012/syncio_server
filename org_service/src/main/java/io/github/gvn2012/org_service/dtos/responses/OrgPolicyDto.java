package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class OrgPolicyDto {
    private UUID id;
    private String title;
    private String content;
    private String version;
    private PolicyCategory category;
    private LocalDate effectiveDate;
    private Boolean active;
    private UUID approvedById;
    
    private Instant createdAt;
    private Instant updatedAt;
}
