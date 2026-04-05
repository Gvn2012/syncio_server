package io.github.gvn2012.notification_service.kafka;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.notification_service.services.EmailSenderService;
import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventConsumer {

    private final EmailSenderService emailSenderService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "email-verification")
    public void consume(EmailVerificationEvent event) {
        if (event == null || event.getEmail() == null || event.getEmail().isBlank()) {
            return;
        }

        if (event.getEventId() != null && notificationRepository.existsByEventId(event.getEventId().toString())) {
            log.info("Duplicate email event dropped: {}", event.getEventId());
            return;
        }

        try {
            emailSenderService.sendVerificationEmail(
                    event.getEmail(),
                    event.getVerificationLink(),
                    event.getVerificationCode()
            );
            saveNotification(event, "SENT");

        } catch (Exception e) {
            log.error("Failed to process email verification event for {}", event.getEmail(), e);
            saveNotification(event, "FAILED");
        }
    }

    private void saveNotification(EmailVerificationEvent event, String status) {
        Notification noti = Notification.builder()
                .eventId(event.getEventId() != null ? event.getEventId().toString() : null)
                .recipientId(event.getUserId())
                .email(event.getEmail())
                .type(NotificationType.EMAIL_VERIFICATION)
                .status(status)
                .build();

        notificationRepository.save(noti);
    }
}
