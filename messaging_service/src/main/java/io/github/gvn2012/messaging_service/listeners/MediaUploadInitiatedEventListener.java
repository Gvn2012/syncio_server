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

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUploadInitiatedEventListener {

    private final IMessagingService messagingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "media.upload.initiated", groupId = "messaging-service-group")
    public void handleMediaUploadInitiated(String payload) {
        log.info("Received media upload initiated event: {}", payload);

        try {
            MediaUploadInitiatedEvent event = objectMapper.readValue(payload, MediaUploadInitiatedEvent.class);
            log.info("Parsed media upload initiated event: {}", event);

            MessageType pendingType = "VIDEO".equalsIgnoreCase(event.getMediaType()) 
                    ? MessageType.VIDEO_PENDING 
                    : MessageType.IMAGE_PENDING;

            MessageRequest messageRequest = MessageRequest.builder()
                    .id(event.getMediaId())
                    .conversationId(event.getConversationId())
                    .senderId(event.getSenderId())
                    .content("")
                    .type(pendingType)
                    .mediaId(event.getMediaId())
                    .mediaContentType(event.getContentType())
                    .mediaSize(event.getSize())
                    .build();

            messagingService.processMessage(messageRequest);
            log.info("Successfully created ephemeral message for imageId: {}", event.getMediaId());

        } catch (Exception e) {
            log.error("Failed to process media upload initiated event", e);
        }
    }
}
