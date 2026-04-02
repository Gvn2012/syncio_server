package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.ModerationQueue;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.ModerationQueueStatus;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.exceptions.NotFoundException;
import io.github.gvn2012.post_service.repositories.ModerationQueueRepository;
import io.github.gvn2012.post_service.repositories.PostRepository;
import io.github.gvn2012.post_service.services.interfaces.IModerationQueueService;
import io.github.gvn2012.post_service.services.kafka.PostEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModerationQueueServiceImpl implements IModerationQueueService {

    private final ModerationQueueRepository queueRepository;
    private final PostRepository postRepository;
    private final PostEventProducer postEventProducer;

    @Override
    @Transactional
    public ModerationQueue reportPost(@NonNull UUID postId, UUID reporterId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
        post.setModerationStatus(PostModerationStatus.REPORTED);
        postRepository.save(post);

        ModerationQueue queue = new ModerationQueue();
        queue.setStatus(ModerationQueueStatus.PENDING);
        ModerationQueue saved = queueRepository.save(queue);
        postEventProducer.publishPostReported(postId, post.getAuthorId(), reporterId);
        return saved;
    }

    @Override
    @Transactional
    public ModerationQueue reportComment(@NonNull UUID commentId, UUID reporterId, String reason) {
        ModerationQueue queue = new ModerationQueue();
        queue.setStatus(ModerationQueueStatus.PENDING);
        return queueRepository.save(queue);
    }

    @Override
    public ModerationQueue getReportById(@NonNull UUID id) {
        return queueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found: " + id));
    }

    @Override
    public List<ModerationQueue> getPendingReports(Pageable pageable) {
        return queueRepository.findByStatus(ModerationQueueStatus.PENDING, pageable);
    }

    @Override
    @Transactional
    public void resolveReport(@NonNull UUID moderationQueueId, UUID adminId, String actionTaken, String adminNotes) {
        ModerationQueue queue = getReportById(moderationQueueId);
        queue.setStatus(ModerationQueueStatus.RESOLVED);
        queueRepository.save(queue);
    }

    @Override
    @Transactional
    public void assignModerator(@NonNull UUID moderationQueueId, UUID moderatorId) {
        ModerationQueue queue = getReportById(moderationQueueId);
        queue.setStatus(ModerationQueueStatus.IN_REVIEW);
        queue.setAssignedTo(moderatorId);
        queueRepository.save(queue);
    }

    @Override
    @Transactional
    public void escalateReport(@NonNull UUID moderationQueueId) {
        ModerationQueue queue = getReportById(moderationQueueId);
        queue.setStatus(ModerationQueueStatus.ESCALATED);
        queueRepository.save(queue);
    }
}
