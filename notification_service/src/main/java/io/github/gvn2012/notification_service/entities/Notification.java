package io.github.gvn2012.notification_service.entities;

import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String eventId;

    private UUID recipientId;
    private UUID actorId;
    private UUID targetId; // Reference to postId, commentId, etc.

    private NotificationType type;
    
    private String title;
    private String message;
    
    @Builder.Default
    private boolean isRead = false;

    private String status;
    private String errorMessage;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String email; // Legacy or for combined notifications
}