package io.github.gvn2012.user_service.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import io.github.gvn2012.user_service.repositories.UserProfilePictureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadConsumer {

    private final UserProfilePictureRepository profilePictureRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "image.uploaded", groupId = "user-service-group")
    public void consume(ImageUploadedEvent event) {
        log.info("Received image uploaded event for imageId: {}", event.getImageId());

        profilePictureRepository.findByExternalId(event.getImageId()).ifPresent(picture -> {
            try {
                picture.setObjectPath(event.getObjectPath());
                picture.setBucketName(event.getBucketName());
                picture.setFileSize(event.getSize());
                picture.setMimeType(event.getContentType());
                
                if (event.getMetadata() != null) {
                    picture.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
                }
                
                profilePictureRepository.save(picture);
                log.info("Updated profile picture for imageId: {}", event.getImageId());
            } catch (Exception e) {
                log.error("Failed to serialize metadata for imageId: {}", event.getImageId(), e);
            }
        });
    }
}
