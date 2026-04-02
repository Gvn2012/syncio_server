package io.github.gvn2012.notification_service.kafka;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.shared.kafka_events.PostActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "post-events", groupId = "notification-post-group")
    public void handlePostActivity(PostActivityEvent event) {
        if (event == null || event.getActorId() == null || event.getAuthorId() == null)
            return;
        if (event.getActorId().equals(event.getAuthorId()))
            return;

        NotificationType type = mapActivityToNotificationType(event.getActivityType());
        if (type == null)
            return;

        String message = formatMessage(event);
        String title = formatTitle(event);

        Notification notification = Notification.builder()
                .recipientId(event.getAuthorId())
                .actorId(event.getActorId())
                .targetId(event.getPostId())
                .type(type)
                .title(title)
                .message(message)
                .status("CREATED")
                .build();

        notificationRepository.save(notification);
        log.info("Created notification for user: {} for activity: {}", event.getAuthorId(), event.getActivityType());
    }

    private NotificationType mapActivityToNotificationType(PostActivityEvent.ActivityType type) {
        return switch (type) {
            case REACTED -> NotificationType.POST_REACTION;
            case COMMENTED -> NotificationType.POST_COMMENT;
            case SHARED -> NotificationType.POST_SHARE;
            default -> null;
        };
    }

    private String formatTitle(PostActivityEvent event) {
        return switch (event.getActivityType()) {
            case REACTED -> "New Reaction";
            case COMMENTED -> "New Comment";
            case SHARED -> "Post Shared";
            default -> "Notification";
        };
    }

    private String formatMessage(PostActivityEvent event) {
        return switch (event.getActivityType()) {
            case REACTED -> "Someone reacted to your post";
            case COMMENTED -> "Someone commented on your post";
            case SHARED -> "Someone shared your post";
            default -> "You have a new notification";
        };
    }
}
