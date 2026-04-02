package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.dtos.mappers.PostTaskMapper;
import io.github.gvn2012.post_service.dtos.requests.PostTaskRequest;
import io.github.gvn2012.post_service.dtos.responses.PostTaskResponse;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostTask;
import io.github.gvn2012.post_service.entities.PostTaskAssignee;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.TaskStatus;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.repositories.PostTaskAssigneeRepository;
import io.github.gvn2012.post_service.repositories.PostTaskRepository;
import io.github.gvn2012.post_service.services.interfaces.IPostTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostTaskServiceImpl implements IPostTaskService {

    private final PostTaskRepository taskRepository;
    private final PostTaskAssigneeRepository assigneeRepository;
    private final PostRepository postRepository;
    private final PostTaskMapper taskMapper;
    private final UserValidationService userValidationService;

    @Override
    @Transactional
    public PostTaskResponse createTask(@NonNull UUID postId, PostTaskRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        
        userValidationService.validateUserCanInteract(post.getAuthorId());
        
        post.setPostCategory(PostCategory.TASK);
        postRepository.save(post);
        
        PostTask task = taskMapper.toEntity(request);
        task.setPost(post);
        task.setPostId(post.getId());
        task.setStatus(TaskStatus.OPEN);
        
        PostTask saved = taskRepository.save(task);
        
        if (request.getAssignees() != null) {
            request.getAssignees().forEach(userId -> addAssignee(saved, userId));
        }
        
        return taskMapper.toResponse(saved);
    }

    private void addAssignee(PostTask task, UUID userId) {
        PostTaskAssignee assignee = new PostTaskAssignee();
        assignee.setTask(task);
        assignee.setUserId(userId);
        assigneeRepository.save(assignee);
    }

    @Override
    public PostTaskResponse getTaskByPostId(UUID postId) {
        PostTask task = taskRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Task not found for post: " + postId));
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void updateTaskStatus(UUID taskId, UUID userId, String status) {
        userValidationService.validateUserCanInteract(userId);
        PostTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void assignUser(UUID taskId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        PostTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        addAssignee(task, userId);
    }

    @Override
    @Transactional
    public void unassignUser(UUID taskId, UUID userId) {
        userValidationService.validateUserCanInteract(userId);
        taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        assigneeRepository.deleteByTaskPostIdAndUserId(taskId, userId);
    }

    @Override
    public List<PostTaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
                .stream().map(taskMapper::toResponse).toList();
    }

    @Override
    public List<PostTaskResponse> getTasksByAssignee(UUID userId, Pageable pageable) {
        return taskRepository.findByAssigneeUserId(userId, pageable)
                .stream().map(taskMapper::toResponse).toList();
    }
}
