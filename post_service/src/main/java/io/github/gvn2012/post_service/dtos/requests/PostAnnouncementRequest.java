package io.github.gvn2012.post_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAnnouncementRequest {
    private String priority;
    private Boolean isPinned;
    private LocalDateTime pinnedUntil;
    private Boolean requiresAcknowledgement;
}
