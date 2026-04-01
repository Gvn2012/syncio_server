package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.ModerationQueue;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IModerationQueueService {
    ModerationQueue reportPost(UUID postId, UUID reporterId, String reason);
    ModerationQueue reportComment(UUID commentId, UUID reporterId, String reason);
    ModerationQueue getReportById(UUID id);
    List<ModerationQueue> getPendingReports(Pageable pageable);
    void resolveReport(UUID moderationQueueId, UUID adminId, String actionTaken, String adminNotes);
    void assignModerator(UUID moderationQueueId, UUID moderatorId);
    void escalateReport(UUID moderationQueueId);
}
