package io.github.gvn2012.image_uploading_service.services.impls;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadBatchRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadBatchResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.models.MediaItem;
import io.github.gvn2012.image_uploading_service.repositories.MediaItemRepository;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import io.github.gvn2012.image_uploading_service.services.interfaces.MessageMediaUploadServiceInterface;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.shared.kafka_events.MediaUploadInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageMediaUploadServiceImpl implements MessageMediaUploadServiceInterface {

    private final GCSServiceInterface gcsService;
    private final MediaItemRepository mediaItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public APIResource<UploadResponse> sendUploadRequest(MessageMediaUploadRequest request) {
        String imageId = UuidCreator.getTimeOrderedEpoch().toString();

        String type = request.getMediaType() != null ? request.getMediaType() : "IMAGE";
        String objectPath = "msg/" + request.getConversationId() + "/" + type + "/" + imageId;

        URL signedUrl;
        if ("VIDEO".equalsIgnoreCase(request.getMediaType())) {
            signedUrl = gcsService.generateResumableUploadUrl(objectPath, request.getFileContentType());
        } else {
            signedUrl = gcsService.generateUploadUrl(objectPath, request.getFileContentType());
        }

        MediaItem item = MediaItem.builder()
                .id(imageId)
                .batchId(request.getBatchId())
                .conversationId(request.getConversationId())
                .fileName(request.getFileName())
                .contentType(request.getFileContentType())
                .mediaType(request.getMediaType())
                .status("PENDING")
                .uploadUrl(signedUrl.toString())
                .metadata(Map.of("senderId", request.getSenderId() != null ? request.getSenderId() : ""))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        mediaItemRepository.save(item);

        MediaUploadInitiatedEvent initiatedEvent = MediaUploadInitiatedEvent.builder()
                .mediaId(imageId)
                .conversationId(request.getConversationId())
                .senderId(request.getSenderId())
                .mediaType(request.getMediaType())
                .fileName(request.getFileName())
                .contentType(request.getFileContentType())
                .size(request.getSize())
                .metadata(item.getMetadata())
                .build();

        kafkaTemplate.send("media.upload.initiated", imageId, initiatedEvent);
        log.info("Published media.upload.initiated event for imageId: {}", imageId);

        UploadResponse response = new UploadResponse(
                imageId,
                signedUrl.toString(),
                "VIDEO".equalsIgnoreCase(request.getMediaType()) ? "POST" : "PUT",
                Map.of("Content-Type", request.getFileContentType()),
                900);

        return APIResource.success(response);
    }

    @Override
    public APIResource<UploadBatchResponse> sendBatchUploadRequest(MessageMediaUploadBatchRequest request) {
        if (request.getRequests() == null || request.getRequests().isEmpty()) {
            return APIResource.success(new UploadBatchResponse(new ArrayList<>()));
        }

        String batchId = UUID.randomUUID().toString();
        List<UploadResponse> responses = new ArrayList<>();

        for (MessageMediaUploadRequest uploadRequest : request.getRequests()) {
            uploadRequest.setBatchId(batchId);
            try {
                APIResource<UploadResponse> response = sendUploadRequest(uploadRequest);
                if (response.getData() != null) {
                    responses.add(response.getData());
                }
            } catch (Exception e) {
                log.error("Error processing batch upload request for file {}: {}", uploadRequest.getFileName(),
                        e.getMessage());
            }
        }

        return APIResource.success(new UploadBatchResponse(responses));
    }
}
