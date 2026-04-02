package io.github.gvn2012.notification_service.services.impls;

import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.notification_service.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotificationsForUser(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);
    }

    @Override
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(UUID recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false).size();
    }
}
