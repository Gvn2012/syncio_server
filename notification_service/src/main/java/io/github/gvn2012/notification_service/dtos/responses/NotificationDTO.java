package io.github.gvn2012.notification_service.dtos.responses;

import io.github.gvn2012.notification_service.entities.enums.NotificationType;
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
public class NotificationDTO {
    private String id;
    private String eventId;
    private UUID recipientId;
    private UUID actorId;
    private UUID targetId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead;
    private String status;
    private LocalDateTime createdAt;
}
