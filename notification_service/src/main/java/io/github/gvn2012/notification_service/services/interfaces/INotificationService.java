package io.github.gvn2012.notification_service.services.interfaces;

import io.github.gvn2012.notification_service.entities.Notification;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    List<Notification> getNotificationsForUser(UUID recipientId, Pageable pageable);
    List<Notification> getUnreadNotificationsForUser(UUID recipientId, Pageable pageable);
    void markAsRead(String notificationId);
    void markAllAsRead(UUID recipientId);
    void deleteNotification(String notificationId);
    long getUnreadCount(UUID recipientId);
}
