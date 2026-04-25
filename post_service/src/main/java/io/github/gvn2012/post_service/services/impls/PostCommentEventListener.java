package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.repositories.PostCommentRepository;
import io.github.gvn2012.post_service.services.events.PostCommentPopularityUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostCommentEventListener {

    private final PostCommentRepository commentRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePopularityUpdate(PostCommentPopularityUpdatedEvent event) {
        log.debug("Updating popularity for comment: {}", event.getCommentId());

        commentRepository.findById(event.getCommentId()).ifPresent(comment -> {
            double popularity = (comment.getReactionCount() * 1.0) + (comment.getReplyCount() * 2.0);
            comment.setPopularity(popularity);
            commentRepository.save(comment);
            log.debug("Comment {} popularity updated to {}", event.getCommentId(), popularity);
        });
    }
}
