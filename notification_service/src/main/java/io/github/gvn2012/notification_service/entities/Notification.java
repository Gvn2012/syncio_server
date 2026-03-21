package io.github.gvn2012.notification_service.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private UUID userId;
    private UUID emailId;

    private String email;

    private String type;

    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}