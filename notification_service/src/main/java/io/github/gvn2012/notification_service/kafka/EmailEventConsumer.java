package io.github.gvn2012.notification_service.kafka;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.notification_service.services.EmailSenderService;
import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final EmailSenderService emailSenderService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "email-verification")
    public void consume(EmailVerificationEvent event) {

        try {
            emailSenderService.sendVerificationEmail(
                    event.getEmail(),
                    event.getVerificationLink()
            );
            saveNotification(event, "SENT");

        } catch (Exception e) {
            saveNotification(event, "FAILED");
        }
    }

    private void saveNotification(EmailVerificationEvent event, String status) {
        Notification noti = Notification.builder()
                .recipientId(event.getUserId())
                .email(event.getEmail())
                .type(io.github.gvn2012.notification_service.entities.enums.NotificationType.EMAIL_VERIFICATION)
                .status(status)
                .build();

        notificationRepository.save(noti);
    }
}