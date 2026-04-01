package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAnnouncementResponse {
    private UUID postId;
    private String priority;
    private Boolean isPinned;
    private LocalDateTime pinnedUntil;
    private Boolean requiresAcknowledgement;
    private Integer readCount;
}
