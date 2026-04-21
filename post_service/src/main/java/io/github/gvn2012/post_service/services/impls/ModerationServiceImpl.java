package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.entities.ModerationQueue;
import io.github.gvn2012.post_service.entities.ModerationQueueAudit;
import io.github.gvn2012.post_service.entities.Post;
import io.github.gvn2012.post_service.entities.enums.ModerationAction;
import io.github.gvn2012.post_service.entities.enums.ModerationContentType;
import io.github.gvn2012.post_service.entities.enums.ModerationQueueStatus;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.exceptions.ForbiddenException;
import io.github.gvn2012.post_service.repositories.ModerationQueueAuditRepository;
import io.github.gvn2012.post_service.repositories.ModerationQueueRepository;
import io.github.gvn2012.post_service.services.interfaces.IModerationService;
import io.github.gvn2012.post_service.utils.ModerationScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationServiceImpl implements IModerationService {

    private final ModerationScanner moderationScanner;
    private final ModerationQueueRepository moderationQueueRepository;
    private final ModerationQueueAuditRepository auditRepository;

    private static final int MAX_VIOLATIONS = 5;
    private static final int VIOLATION_WINDOW_DAYS = 3;

    @Override
    @Transactional
    public void moderatePost(UUID userId, Post post) {
        ModerationScanner.ScanResult contentResult = moderationScanner.scanAndCensor(post.getContent());
        ModerationScanner.ScanResult htmlResult = moderationScanner.scanAndCensorHtml(post.getContentHtml());

        if (!contentResult.hasViolations()) {
            return;
        }

        log.info("Inappropriate words detected for user {}: {}", userId, contentResult.getDetectedWords());

        LocalDateTime threshold = LocalDateTime.now().minusDays(VIOLATION_WINDOW_DAYS);
        long violationCount = moderationQueueRepository.countByAuthorIdAndCreatedAtAfter(userId, threshold);

        if (violationCount >= MAX_VIOLATIONS) {
            log.warn("User {} blocked due to excessive violations ({} in 3 days)", userId, violationCount);
            throw new ForbiddenException(
                    "You are blocked from posting due to excessive content violations. (Limit: 5 in 3 days)");
        }

        post.setContent(contentResult.getSanitizedContent());
        post.setContentHtml(htmlResult.getSanitizedContent());
        post.setModerationStatus(PostModerationStatus.FLAGGED);

        logViolation(userId, post, String.join(", ", contentResult.getDetectedWords()));
    }

    private void logViolation(UUID userId, Post post, String wordsFound) {
        ModerationQueue queue = new ModerationQueue();
        queue.setAuthorId(userId);
        queue.setContentId(post.getId());
        queue.setContentType(ModerationContentType.POST);
        queue.setStatus(ModerationQueueStatus.PENDING);
        queue.setAutoFlagReason("Auto-moderated: detected inappropriate words: " + wordsFound);
        queue.setContentSnapshot(post.getContent());

        ModerationQueue savedQueue = moderationQueueRepository.save(queue);

        ModerationQueueAudit audit = new ModerationQueueAudit();
        audit.setModerationQueue(savedQueue);
        audit.setActorId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        audit.setAction(ModerationAction.FLAG);
        audit.setNewStatus(ModerationQueueStatus.PENDING.name());
        audit.setNote("Automated content flag for inappropriate words.");

        auditRepository.save(audit);
    }
}
