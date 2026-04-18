package io.github.gvn2012.notification_service.repositories;

import io.github.gvn2012.notification_service.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsRead(UUID recipientId, boolean isRead, Pageable pageable);

    List<Notification> findByRecipientIdAndIsRead(UUID recipientId, boolean isRead);

    long countByRecipientIdAndIsRead(UUID recipientId, boolean isRead);

    List<Notification> findByStatus(String status);

    boolean existsByEventId(String eventId);
}