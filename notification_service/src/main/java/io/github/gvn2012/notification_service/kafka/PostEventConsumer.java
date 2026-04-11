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
    private final io.github.gvn2012.notification_service.clients.RelationshipClient relationshipClient;

    @KafkaListener(topics = "post-events-v2", groupId = "notification-post-group")
    public void handlePostActivity(PostActivityEvent event) {
        if (event == null) return;
        
        if (event.getActivityType() == PostActivityEvent.ActivityType.CREATED) {
            handlePostCreated(event);
            return;
        }

        if (event.getActorId() == null || event.getAuthorId() == null)
            return;
        if (event.getActorId().equals(event.getAuthorId()))
            return;

        if (event.getEventId() != null && notificationRepository.existsByEventId(event.getEventId().toString())) {
            log.info("Duplicate event dropped: {}", event.getEventId());
            return;
        }

        NotificationType type = mapActivityToNotificationType(event.getActivityType());
        if (type == null)
            return;

        String message = formatMessage(event);
        String title = formatTitle(event);

        Notification notification = Notification.builder()
                .eventId(event.getEventId() != null ? event.getEventId().toString() : null)
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

    private void handlePostCreated(PostActivityEvent event) {
        relationshipClient.getAudience(event.getAuthorId())
            .subscribe(audience -> {
                audience.forEach(recipientId -> {
                    Notification notification = Notification.builder()
                        .eventId(event.getEventId() != null ? event.getEventId().toString() + "_" + recipientId : null)
                        .recipientId(recipientId)
                        .actorId(event.getAuthorId())
                        .targetId(event.getPostId())
                        .type(NotificationType.POST_CREATED)
                        .title("New post from " + (event.getActorName() != null ? event.getActorName() : "a friend"))
                        .message(formatCreatedMessage(event))
                        .status("CREATED")
                        .build();
                    notificationRepository.save(notification);
                });
                log.info("Created {} notifications for post: {}", audience.size(), event.getPostId());
            });
    }

    private String formatCreatedMessage(PostActivityEvent event) {
        String actor = event.getActorName() != null ? event.getActorName() : "User";
        String category = event.getPostCategory() != null ? event.getPostCategory().toLowerCase() : "post";
        
        return switch (category) {
            case "poll" -> actor + " has started a new poll";
            case "event" -> actor + " has scheduled an event";
            case "task" -> actor + " has created a task";
            case "announcement" -> actor + " has made an announcement";
            default -> actor + " shared a new story";
        };
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
