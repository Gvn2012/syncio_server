package io.github.gvn2012.notification_service.services.impls;

import io.github.gvn2012.notification_service.dtos.responses.NotificationDTO;
import io.github.gvn2012.notification_service.dtos.responses.PageResponse;
import io.github.gvn2012.notification_service.entities.Notification;
import io.github.gvn2012.notification_service.repositories.NotificationRepository;
import io.github.gvn2012.notification_service.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getNotificationsForUser(UUID recipientId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository.findByRecipientId(recipientId, pageable);
        return mapToPageResponse(notificationPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getUnreadNotificationsForUser(UUID recipientId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository.findByRecipientIdAndIsRead(recipientId, false,
                pageable);
        return mapToPageResponse(notificationPage);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsRead(recipientId,
                false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void markAsReadInBatch(List<String> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    private PageResponse<NotificationDTO> mapToPageResponse(Page<Notification> page) {
        List<NotificationDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<NotificationDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .recipientId(notification.getRecipientId())
                .actorId(notification.getActorId())
                .targetId(notification.getTargetId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
