package io.github.gvn2012.user_service.services.kafka;

import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import io.github.gvn2012.user_service.repositories.UserProfilePictureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadConsumer {

    private final UserProfilePictureRepository profilePictureRepository;

    @Value("${gcp.storage.public-url-prefix:https://storage.googleapis.com}")
    private String publicUrlPrefix;

    @Transactional
    @KafkaListener(topics = "image.uploaded", groupId = "user-service-group")
    public void consume(ImageUploadedEvent event) {
        log.info("Received image uploaded event for imageId: {}", event.getImageId());

        profilePictureRepository.findByExternalId(event.getImageId()).ifPresent(picture -> {
            picture.setObjectPath(event.getObjectPath());
            picture.setBucketName(event.getBucketName());
            picture.setFileSize(event.getSize());
            picture.setMimeType(event.getContentType());
            
            profilePictureRepository.save(picture);
            log.info("Updated profile picture for imageId: {} with path: {}", event.getImageId(), event.getObjectPath());
        });
    }
}
