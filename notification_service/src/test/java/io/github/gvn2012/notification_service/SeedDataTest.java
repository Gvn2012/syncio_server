package io.github.gvn2012.notification_service;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class SeedDataTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void seedNotifications() {
        UUID recipientId = UUID.fromString("0a5401c0-9d78-14d6-819d-78887a2f0001");

        List<Notification> samples = new ArrayList<>();

        String[] titles = {
                "New Like", "New Comment", "New Mention", "Follow Request",
                "Task Assigned", "System Maintenance", "Post Featured", "Direct Message"
        };

        String[] messages = {
                "John Doe liked your recent post about Spring Boot.",
                "Sarah Jenkins commented: 'Great insights, thanks for sharing!'",
                "You were mentioned in a discussion about SyncIO architecture.",
                "Alex Smith requested to follow you.",
                "A new task 'Update documentation' has been assigned to you.",
                "System will be down for maintenance tonight at 2 AM.",
                "Your post 'Modern UI Patterns' is trending in the organization.",
                "Hey, let's catch up regarding the next sprint goals."
        };

        NotificationType[] types = NotificationType.values();

        for (int i = 0; i < 25; i++) {
            NotificationType type = types[i % types.length];
            String title = titles[i % titles.length];
            String message = messages[i % messages.length];

            Notification notification = Notification.builder()
                    .eventId(UUID.randomUUID().toString())
                    .recipientId(recipientId)
                    .actorId(UUID.randomUUID())
                    .targetId(UUID.randomUUID())
                    .type(type)
                    .title(title + " #" + (i + 1))
                    .message(message)
                    .isRead(i >= 10) // 10 unread, 15 read
                    .status("SENT")
                    .createdAt(LocalDateTime.now().minusHours(i))
                    .build();

            samples.add(notification);
        }

        notificationRepository.saveAll(samples);
        System.out.println("Successfully seeded 25 notifications for user: " + recipientId);
    }
}
