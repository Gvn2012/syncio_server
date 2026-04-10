package io.github.gvn2012.notification_service.kafka;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.notification_service.services.impls.EmailSenderService;
import io.github.gvn2012.shared.kafka_events.OrgCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgEventComsumer {

    private final EmailSenderService emailSenderService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "org.created")
    public void consume(OrgCreatedEvent event) {
        log.info("Event get {}", event);
        if (event.getOwnerId() == null || event.getOwnerId().isBlank()) {
            log.error("Invalid event: {}", event);
            return;
        }

        if (event.getEventId() != null && notificationRepository.existsByEventId(event.getEventId().toString())) {
            log.info("Duplicate email event dropped: {}", event.getEventId());
            return;
        }

        try {
            emailSenderService.sendOrganizationWelcomeEmail(event.getEmail(), event.getName(), event.getOrgId());
            saveNotification(event, "SENT");

        } catch (Exception e) {
            log.error("Failed to process email verification event for {}", event.getEmail(), e);
            saveNotification(event, "FAILED");
        }
    }

    private void saveNotification(OrgCreatedEvent event, String status) {
        Notification noti = Notification.builder()
                .eventId(event.getEventId() != null ? event.getEventId().toString() : null)
                .recipientId(UUID.fromString(event.getOwnerId()))
                .email(event.getEmail())
                .type(NotificationType.ORG_CREATED)
                .status(status)
                .build();

        notificationRepository.save(noti);
    }
}
