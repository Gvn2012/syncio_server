package io.github.gvn2012.messaging_service.listeners;

import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import io.github.gvn2012.messaging_service.models.MediaItem;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadedEventListener {

    private final IMessagingService messagingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "media.uploaded.msg", groupId = "messaging-service-group")
    public void handleMediaUploaded(String payload) {
        log.info("Received media uploaded event string payload: {}", payload);

        try {
            ImageUploadedEvent event = objectMapper.readValue(payload, ImageUploadedEvent.class);
            log.info("Parsed media uploaded event: {}", event);

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
            } else if ("AUDIO".equals(typeStr)) {
                messageType = MessageType.AUDIO;
            }

            String resolvedDownloadUrl = event.getDownloadUrl() != null
                    ? event.getDownloadUrl()
                    : event.getObjectPath();

            MessageRequest messageRequest = MessageRequest.builder()
                    .batchId(event.getBatchId())
                    .conversationId(conversationId)
                    .senderId(senderId)
                    .content("")
                    .type(messageType)
                    .mediaItems(List.of(MediaItem.builder()
                            .id(mediaId)
                            .batchId(event.getBatchId())
                            .conversationId(conversationId)
                            .fileName(event.getFileName())
                            .contentType(event.getContentType())
                            .mediaType(typeStr)
                            .status("UPLOADED")
                            .downloadUrl(resolvedDownloadUrl)
                            .size(event.getSize())
                            .build()))
                    .build();

            messagingService.processMessage(messageRequest);
            log.info("Successfully processed media message creation for conversation: {}", conversationId);

        } catch (Exception e) {
            log.error("Failed to process media message creation", e);
        }
    }
}
