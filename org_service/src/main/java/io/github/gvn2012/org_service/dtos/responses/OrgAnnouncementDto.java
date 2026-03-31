package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.AnnouncementPriority;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrgAnnouncementDto {
    private UUID id;
    private String title;
    private String content;
    private UUID authorId;
    
    private UUID departmentId;
    private String departmentName;

    private AnnouncementPriority priority;
    private Boolean pinned;
    
    private Instant publishedAt;
    private Instant expiresAt;

    private Instant createdAt;
    private Instant updatedAt;
}
