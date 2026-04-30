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

    private static final Integer MIN_PATH_LENGTH = 4;

    private final IMessagingService messagingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "media.uploaded.msg", groupId = "messaging-service-group")
    public void handleMediaUploaded(String payload) {

        try {
            ImageUploadedEvent event = objectMapper.readValue(payload, ImageUploadedEvent.class);

            String[] parts = processObjectPath(event.getObjectPath(), "/");
            if (parts.length < MIN_PATH_LENGTH)
                throw new RuntimeException("Invalid object path");

            String conversationId = parts[1];
            String typeStr = parts[2].toUpperCase();
            String mediaId = parts[3];

            Map<String, Object> metadata = event.getMetadata();
            String senderId = metadata != null && metadata.containsKey("senderId")
                    ? metadata.get("senderId").toString()
                    : "unknown";

            MessageType messageType = processMediaType(typeStr);

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

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private String[] processObjectPath(String objectPath, String separator) {
        String[] parts = objectPath.split(separator);
        if (parts.length < MIN_PATH_LENGTH)
            throw new RuntimeException("Invalid object path");
        return parts;
    }

    private MessageType processMediaType(String typeStr) {
        return switch (typeStr.toUpperCase()) {
            case "VIDEO" -> MessageType.VIDEO;
            case "AUDIO" -> MessageType.AUDIO;
            default -> MessageType.IMAGE;
        };
    }
}
