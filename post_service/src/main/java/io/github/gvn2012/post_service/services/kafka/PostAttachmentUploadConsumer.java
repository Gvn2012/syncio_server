package io.github.gvn2012.post_service.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
import io.github.gvn2012.post_service.repositories.PostMediaAttachmentRepository;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAttachmentUploadConsumer {

    private final PostMediaAttachmentRepository attachmentRepository;
    private final ObjectMapper objectMapper;

    @Value("${gcp.storage.public-url-prefix:https://storage.googleapis.com}")
    private String publicUrlPrefix;

    @Transactional
    @KafkaListener(topics = "image.uploaded", groupId = "post-service-group")
    public void consume(ImageUploadedEvent event) {
        if (event.getObjectPath() == null || !event.getObjectPath().startsWith("post_img/")) {
            log.debug("Ignoring non-post upload event: {}", event.getImageId());
            return;
        }

        log.info("Received post attachment upload event for imageId: {}", event.getImageId());

        attachmentRepository.findByExternalId(event.getImageId()).ifPresentOrElse(attachment -> {
            try {
                attachment.setObjectPath(event.getObjectPath());
                attachment.setBucketName(event.getBucketName());
                attachment.setFileSize(event.getSize());
                attachment.setUrl(
                        String.format("%s/%s/%s", publicUrlPrefix, event.getBucketName(), event.getObjectPath()));

                if (event.getContentType() != null) {
                    attachment.setMimeType(event.getContentType());
                }

                if (event.getMetadata() != null) {
                    attachment.setMetadata(objectMapper.writeValueAsString(event.getMetadata()));
                }

                attachment.setUploadStatus(AttachmentUploadStatus.SUCCESSFUL);
                attachment.setUploadedAt(LocalDateTime.now());
                attachmentRepository.save(attachment);

                log.info("Finalized post attachment for imageId: {}, postId: {}",
                        event.getImageId(), attachment.getPost().getId());
            } catch (Exception e) {
                log.error("Error processing post attachment for imageId: {}", event.getImageId(), e);
            }
        }, () -> {
            log.warn("Post attachment not found for imageId: {}. Upload may still be in progress.",
                    event.getImageId());
            throw new RuntimeException("Post attachment record not yet persistent for ID: " + event.getImageId());
        });
    }
}
