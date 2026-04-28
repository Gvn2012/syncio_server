package io.github.gvn2012.messaging_service.listeners;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadedEventListener {

    private final IMessagingService messagingService;

    @KafkaListener(topics = "media.uploaded.msg", groupId = "messaging-service-group")
    public void handleMediaUploaded(ImageUploadedEvent event) {
        log.info("Received media uploaded event: {}", event);

        try {
            String objectPath = event.getObjectPath();
            String[] parts = objectPath.split("/");
            if (parts.length < 4) {
                log.warn("Invalid object path for message media: {}", objectPath);
                return;
            }

            String conversationId = parts[1];
            String typeStr = parts[2].toUpperCase();
            String mediaId = parts[3];

            Map<String, Object> metadata = event.getMetadata();
            String senderId = metadata != null && metadata.containsKey("senderId")
                    ? metadata.get("senderId").toString()
                    : "unknown";

            MessageType messageType = MessageType.IMAGE;
            if ("VIDEO".equals(typeStr)) {
                messageType = MessageType.VIDEO;
            }

            MessageRequest messageRequest = MessageRequest.builder()
                    .id(mediaId)
                    .conversationId(conversationId)
                    .senderId(senderId)
                    .content("")
                    .type(messageType)
                    .mediaId(mediaId)
                    .mediaUrl(objectPath)
                    .mediaSize(event.getSize())
                    .mediaContentType(event.getContentType())
                    .build();

            messagingService.processMessage(messageRequest);
            log.info("Successfully processed media message creation for conversation: {}", conversationId);

        } catch (Exception e) {
            log.error("Failed to process media message creation", e);
        }
    }
}
