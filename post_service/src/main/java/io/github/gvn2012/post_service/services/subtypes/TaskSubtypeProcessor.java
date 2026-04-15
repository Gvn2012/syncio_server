package io.github.gvn2012.post_service.services.subtypes;

import io.github.gvn2012.post_service.dtos.mappers.PostTaskMapper;
import io.github.gvn2012.post_service.dtos.requests.PostCreateRequest;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.PostTask;
import io.github.gvn2012.post_service.entities.PostTaskAssignee;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.repositories.PostTaskAssigneeRepository;
import io.github.gvn2012.post_service.repositories.PostTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskSubtypeProcessor implements PostSubtypeProcessor {

    private final PostTaskMapper postTaskMapper;
    private final PostTaskRepository postTaskRepository;
    private final PostTaskAssigneeRepository postTaskAssigneeRepository;

    @Override
    public PostCategory supportedCategory() {
        return PostCategory.TASK;
    }

    @Override
    public void process(Post post, PostCreateRequest request) {
        if (request.getTask() == null) return;
        PostTask task = postTaskMapper.toEntity(request.getTask());
        task.setPost(post);
        PostTask saved = postTaskRepository.save(task);
        if (request.getTask().getAssignees() != null) {
            List<PostTaskAssignee> assignees = request.getTask().getAssignees().stream()
                    .map(uid -> new PostTaskAssignee(null, saved, uid, LocalDateTime.now()))
                    .toList();
            if (!assignees.isEmpty()) {
                postTaskAssigneeRepository.saveAll(assignees);
                saved.setAssignees(new LinkedHashSet<>(assignees));
            }
        }
        post.setTask(saved);
    }
}
