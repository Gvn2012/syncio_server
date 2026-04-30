package io.github.gvn2012.messaging_service.dtos;

import io.github.gvn2012.messaging_service.models.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSummaryResponse {
    private String id;
    private String name;
    private String groupAvatar;
    private String description;
    private List<String> adminIds;
    private int maxSize;
    private int participantCount;
    private List<ParticipantPreview> participantPreviews;
    private ConversationType type;
    private MessageResponse lastMessage;
    private int unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
