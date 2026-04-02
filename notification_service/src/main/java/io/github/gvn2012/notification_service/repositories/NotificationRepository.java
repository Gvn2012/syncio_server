package io.github.gvn2012.notification_service.repositories;

import io.github.gvn2012.notification_service.entities.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(UUID recipientId, boolean isRead);

    List<Notification> findByStatus(String status);
    
    boolean existsByEventId(String eventId);
}