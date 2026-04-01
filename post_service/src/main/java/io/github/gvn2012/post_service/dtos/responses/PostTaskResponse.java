package io.github.gvn2012.post_service.dtos.responses;

import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostTaskResponse {
    private UUID id;
    private UUID postId;
    private String title;
    private String description;
    private String priority;
    private TaskStatus status;
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private List<UUID> assignees;
}
