package io.github.gvn2012.post_service.controllers;

import io.github.gvn2012.post_service.dtos.APIResource;
import io.github.gvn2012.post_service.dtos.requests.PostTaskRequest;
import io.github.gvn2012.post_service.dtos.responses.PostTaskResponse;
import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import io.github.gvn2012.post_service.services.interfaces.IPostTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/tasks")
@RequiredArgsConstructor
public class PostTaskController {

    private final IPostTaskService taskService;

    @PostMapping("/{pid}")
    public ResponseEntity<APIResource<PostTaskResponse>> createTask(
            @PathVariable("pid") UUID postId,
            @RequestBody PostTaskRequest task) {
        return ResponseEntity.ok(APIResource.ok("Task created", taskService.createTask(postId, task)));
    }

    @GetMapping("/{pid}")
    public ResponseEntity<APIResource<PostTaskResponse>> getTask(@PathVariable("pid") UUID postId) {
        return ResponseEntity.ok(APIResource.ok("Task retrieved", taskService.getTaskByPostId(postId)));
    }

    @PatchMapping("/{tkid}/status/{status}")
    public ResponseEntity<Void> updateTaskStatus(
            @PathVariable("tkid") UUID taskId,
            @PathVariable String status,
            @RequestHeader("X-User-ID") UUID userId) {
        taskService.updateTaskStatus(taskId, userId, status);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tkid}/assign/{uid}")
    public ResponseEntity<Void> assignUser(
            @PathVariable("tkid") UUID taskId,
            @PathVariable("uid") UUID userId) {
        taskService.assignUser(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tkid}/unassign/{uid}")
    public ResponseEntity<Void> unassignUser(
            @PathVariable("tkid") UUID taskId,
            @PathVariable("uid") UUID userId) {
        taskService.unassignUser(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignee/{uid}")
    public ResponseEntity<APIResource<List<PostTaskResponse>>> getTasksByAssignee(
            @PathVariable("uid") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Tasks by assignee",
                taskService.getTasksByAssignee(userId, PageRequest.of(page, size))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<APIResource<List<PostTaskResponse>>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResource.ok("Tasks by status",
                taskService.getTasksByStatus(status, PageRequest.of(page, size))));
    }
}
