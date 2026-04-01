package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.EventParticipantStatus;
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
public class PostEventParticipantResponse {
    private UUID id;
    private UUID eventId;
    private UUID userId;
    private EventParticipantStatus status;
    private LocalDateTime respondedAt;
}
