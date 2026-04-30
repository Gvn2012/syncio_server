package io.github.gvn2012.messaging_service.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.messaging_service.dtos.MessageRequest;
import io.github.gvn2012.messaging_service.models.enums.MessageType;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import io.github.gvn2012.shared.kafka_events.MediaUploadInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.gvn2012.messaging_service.models.MediaItem;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadInitiatedEventListener {

    private final IMessagingService messagingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "media.upload.initiated", groupId = "messaging-service-group")
    public void handleMediaUploadInitiated(String payload) {

        try {
            MediaUploadInitiatedEvent event = objectMapper.readValue(payload, MediaUploadInitiatedEvent.class);

            MessageType pendingType = getPendingType(event.getMediaType());

            MessageRequest messageRequest = buildMessageRequest(event, pendingType);

            messagingService.processMessage(messageRequest);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private MessageType getPendingType(String mediaType) {
        return switch (mediaType.toUpperCase()) {
            case "VIDEO" -> MessageType.VIDEO_PENDING;
            case "AUDIO" -> MessageType.AUDIO_PENDING;
            default -> MessageType.IMAGE_PENDING;
        };
    }

    private MessageRequest buildMessageRequest(MediaUploadInitiatedEvent event, MessageType type) {
        return MessageRequest.builder()
                .batchId(event.getBatchId())
                .conversationId(event.getConversationId())
                .senderId(event.getSenderId())
                .content("")
                .type(type)
                .mediaItems(List.of(MediaItem.builder()
                        .id(event.getMediaId())
                        .batchId(event.getBatchId())
                        .conversationId(event.getConversationId())
                        .fileName(event.getFileName())
                        .contentType(event.getContentType())
                        .mediaType(event.getMediaType())
                        .status("INITIATED")
                        .uploadUrl(event.getUploadUrl())
                        .size(event.getSize())
                        .createdAt(Instant.now())
                        .build()))
                .build();
    }
}
