package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.AnnouncementPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateOrgAnnouncementRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private UUID departmentId;
    
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;
    
    private Boolean pinned = false;
    
    private Instant publishedAt;
    
    private Instant expiresAt;
}
