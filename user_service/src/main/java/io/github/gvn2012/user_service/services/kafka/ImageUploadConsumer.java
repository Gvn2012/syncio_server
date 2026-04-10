package io.github.gvn2012.user_service.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import io.github.gvn2012.shared.kafka_events.UserSearchEvent;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.repositories.UserProfilePictureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadConsumer {

    private final UserProfilePictureRepository profilePictureRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${gcp.storage.public-url-prefix:https://storage.googleapis.com}")
    private String publicUrlPrefix;

    @Transactional
    @KafkaListener(topics = "image.uploaded", groupId = "user-service-group")
    public void consume(ImageUploadedEvent event) {
        log.info("Received image uploaded event for imageId: {}", event.getImageId());

        profilePictureRepository.findByExternalId(event.getImageId()).ifPresentOrElse(picture -> {
            try {
                picture.setObjectPath(event.getObjectPath());
                picture.setBucketName(event.getBucketName());
                picture.setFileSize(event.getSize());
                picture.setMimeType(event.getContentType());
                picture.setUrl(String.format("%s/%s/%s", publicUrlPrefix, event.getBucketName(), event.getObjectPath()));

                if (event.getMetadata() != null) {
                    picture.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
                }

                profilePictureRepository.save(picture);
                log.info("Successfully finalized profile picture for imageId: {}, url: {}", event.getImageId(), picture.getUrl());

                // Notify Search Service
                User user = picture.getUserProfile().getUser();
                UserSearchEvent searchEvent = UserSearchEvent.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .fullName(String.format("%s %s", user.getFirstName(), user.getLastName()))
                        .avatarUrl(picture.getUrl())
                        .avatarPath(picture.getObjectPath())
                        .operationType(UserSearchEvent.OperationType.UPSERT)
                        .build();

                kafkaTemplate.send("user-search-indexing", user.getId().toString(), searchEvent);
                log.info("Sent index update for user: {} with avatar path: {}", user.getId(), picture.getObjectPath());

            } catch (Exception e) {
                log.error("Error processing finalized profile picture for imageId: {}", event.getImageId(), e);
            }
        }, () -> {
            log.warn("Profile picture not found for imageId: {}. Registration might still be in progress. Retrying...", event.getImageId());
            throw new RuntimeException("Profile picture record not yet persistent for ID: " + event.getImageId());
        });
    }
}
