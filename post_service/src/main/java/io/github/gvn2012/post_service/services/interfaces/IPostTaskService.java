package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.dtos.requests.PostTaskRequest;
import io.github.gvn2012.post_service.dtos.responses.PostTaskResponse;
import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IPostTaskService {
    PostTaskResponse createTask(UUID postId, PostTaskRequest request);
    PostTaskResponse getTaskByPostId(UUID postId);
    void updateTaskStatus(UUID taskId, UUID userId, String status);
    void assignUser(UUID taskId, UUID userId);
    void unassignUser(UUID taskId, UUID userId);
    List<PostTaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable);
    List<PostTaskResponse> getTasksByAssignee(UUID userId, Pageable pageable);
}
