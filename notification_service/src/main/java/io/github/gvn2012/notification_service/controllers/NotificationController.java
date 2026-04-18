package io.github.gvn2012.notification_service.controllers;

import io.github.gvn2012.notification_service.dtos.APIResource;
import io.github.gvn2012.notification_service.dtos.responses.NotificationDTO;
import io.github.gvn2012.notification_service.dtos.responses.PageResponse;
import io.github.gvn2012.notification_service.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.entities.enums.NotificationType;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    @PostMapping("/seed")
    public ResponseEntity<String> seedNotifications(@RequestParam UUID userId) {
        List<Notification> samples = new ArrayList<>();
        String[] titles = {"New Like", "New Comment", "New Mention", "Follow Request", "Task Assigned", "System Alert"};
        String[] messages = {
            "Someone liked your post.",
            "Someone commented on your post.",
            "You were mentioned in a post.",
            "You have a new follow request.",
            "A new task has been assigned to you.",
            "System maintenance scheduled."
        };
        NotificationType[] types = NotificationType.values();

        for (int i = 0; i < 25; i++) {
            samples.add(Notification.builder()
                .eventId(UUID.randomUUID().toString())
                .recipientId(userId)
                .actorId(UUID.randomUUID())
                .targetId(UUID.randomUUID())
                .type(types[i % types.length])
                .title(titles[i % titles.length] + " #" + (i + 1))
                .message(messages[i % messages.length])
                .isRead(i >= 15)
                .status("SENT")
                .createdAt(LocalDateTime.now().minusHours(i))
                .build());
        }
        notificationRepository.saveAll(samples);
        return ResponseEntity.ok("Successfully seeded 25 notifications for " + userId);
    }

    private final INotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<APIResource<PageResponse<NotificationDTO>>> getNotifications(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationDTO> response = notificationService.getNotificationsForUser(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(APIResource.ok("Notifications retrieved successfully", response));
    }

    @GetMapping("/unread")
    public ResponseEntity<APIResource<PageResponse<NotificationDTO>>> getUnreadNotifications(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationDTO> response = notificationService.getUnreadNotificationsForUser(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(APIResource.ok("Unread notifications retrieved successfully", response));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<APIResource<Long>> getUnreadCount(@RequestHeader("X-User-ID") UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(APIResource.ok("Unread count retrieved successfully", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<APIResource<Void>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(APIResource.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<APIResource<Void>> markAllAsRead(@RequestHeader("X-User-ID") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(APIResource.ok("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResource<Void>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(APIResource.ok("Notification deleted successfully", null));
    }
}
