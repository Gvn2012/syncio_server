package io.github.gvn2012.notification_service.services.interfaces;

import io.github.gvn2012.notification_service.dtos.responses.NotificationDTO;
import io.github.gvn2012.notification_service.dtos.responses.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    PageResponse<NotificationDTO> getNotificationsForUser(UUID recipientId, Pageable pageable);

    PageResponse<NotificationDTO> getUnreadNotificationsForUser(UUID recipientId, Pageable pageable);

    void markAsRead(String notificationId);

    void markAsReadInBatch(List<String> notificationIds);

    void markAllAsRead(UUID recipientId);

    void deleteNotification(String notificationId);

    long getUnreadCount(UUID recipientId);
}
