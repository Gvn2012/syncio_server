package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.AnnouncementPriority;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UpdateOrgAnnouncementRequest {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;

    private UUID departmentId;
    
    private AnnouncementPriority priority;
    
    private Boolean pinned;
    
    private Instant publishedAt;
    
    private Instant expiresAt;
}
