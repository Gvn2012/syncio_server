package io.github.gvn2012.post_service.services.interfaces;

import io.github.gvn2012.post_service.entities.ModerationQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface IModerationQueueService {
    ModerationQueue reportPost(@NonNull UUID postId, UUID reporterId, String reason);

    ModerationQueue reportComment(@NonNull UUID commentId, UUID reporterId, String reason);

    ModerationQueue getReportById(@NonNull UUID id);

    List<ModerationQueue> getPendingReports(Pageable pageable);

    void resolveReport(@NonNull UUID moderationQueueId, UUID adminId, String actionTaken, String adminNotes);

    void assignModerator(@NonNull UUID moderationQueueId, UUID moderatorId);

    void escalateReport(@NonNull UUID moderationQueueId);
}
