package io.github.gvn2012.image_uploading_service.services.impls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.shared.kafka_events.ImageUploadedEvent;
import io.github.gvn2012.image_uploading_service.dtos.requests.SignedUrlRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadBatchRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.SignedUrlResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadBatchResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.models.UploadAudit;
import io.github.gvn2012.image_uploading_service.repositories.UploadAuditRepository;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import io.github.gvn2012.image_uploading_service.services.interfaces.UploadServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadServiceInterface {

    private final GCSServiceInterface gcsService;
    private final UploadAuditRepository uploadAuditRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    @Override
    public APIResource<UploadResponse> sendUploadRequest(UploadRequest request) {

        String imageId = UUID.randomUUID().toString();
        String folder = (request.getFolder() != null && !request.getFolder().isBlank()) ? request.getFolder() : "prl_img";
        String objectPath = folder + "/" + imageId + "-" + request.getFileName();

        URL signedUrl = gcsService.generateUploadUrl(objectPath, request.getFileContentType());

        UploadAudit audit = UploadAudit.builder()
                .imageId(imageId)
                .fileName(request.getFileName())
                .contentType(request.getFileContentType())
                .status("PENDING")
                .uploadUrl(signedUrl.toString())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        uploadAuditRepository.save(audit);

        UploadResponse response = new UploadResponse(
                imageId,
                signedUrl.toString(),
                "PUT",
                Map.of("Content-Type", request.getFileContentType()),
                900);

        return APIResource.success(response);
    }

    @Override
    @Transactional
    public APIResource<UploadBatchResponse> sendBatchUploadRequest(UploadBatchRequest request) {
        List<UploadResponse> responses = request.getRequests().stream()
                .map(req -> sendUploadRequest(req).getData())
                .toList();

        return APIResource.success(new UploadBatchResponse(responses));
    }

    @Override
    public APIResource<UploadConfirmResponse> confirmUpload(UploadConfirmRequest request) {
        UploadAudit audit = uploadAuditRepository.findByImageId(request.getImageId())
                .orElseThrow(() -> new RuntimeException("Upload not found"));

        UploadConfirmResponse response = new UploadConfirmResponse(
                audit.getImageId(),
                audit.getStatus());

        return APIResource.success(response);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(Map<String, Object> body) {
        try {
            Map<String, Object> message = (Map<String, Object>) body.get("message");
            String data = (String) message.get("data");

            String decoded = new String(Base64.getDecoder().decode(data));
            JsonNode json = objectMapper.readTree(decoded);

            String objectName = json.get("name").asText();
            String bucketName = json.get("bucket").asText();

            log.info("Processing GCS event for object: {}", objectName);

            if (objectName.endsWith("/") || !objectName.contains("/")) {
                log.info("Skipping directory or root object: {}", objectName);
                return;
            }

            String[] parts = objectName.split("/");
            String lastSegment = parts[parts.length - 1];

            if (lastSegment.length() < 36) {
                log.warn("Skipping object with invalid ID format in last segment: {}", objectName);
                return;
            }

            String imageId = lastSegment.substring(0, 36);
            log.info("Extracted imageId: {} for object: {}", imageId, objectName);

            uploadAuditRepository.findByImageId(imageId).ifPresentOrElse(audit -> {
                log.info("Updating audit for imageId: {}", imageId);
                audit.setStatus("COMPLETED");
                audit.setUpdatedAt(Instant.now());
                uploadAuditRepository.save(audit);
            }, () -> {
                log.warn("UploadAudit not found for imageId: {}", imageId);
            });

            Map<String, Object> metadata = new HashMap<>();
            if (json.has("etag")) metadata.put("etag", json.get("etag").asText());
            if (json.has("md5Hash")) metadata.put("md5Hash", json.get("md5Hash").asText());
            if (json.has("timeCreated")) metadata.put("timeCreated", json.get("timeCreated").asText());
            if (json.has("updated")) metadata.put("updated", json.get("updated").asText());
            
            if (json.has("metadata")) {
                metadata.putAll(objectMapper.convertValue(json.get("metadata"), Map.class));
            }

            ImageUploadedEvent event = ImageUploadedEvent.builder()
                    .imageId(imageId)
                    .objectPath(objectName)
                    .bucketName(bucketName)
                    .contentType(json.has("contentType") ? json.get("contentType").asText() : null)
                    .size(json.has("size") ? json.get("size").asLong() : null)
                    .metadata(metadata)
                    .build();

            String topic = resolveTopicByPrefix(objectName);
            kafkaTemplate.send(topic, imageId, event);
            log.info("Published event to topic: {} for imageId: {}", topic, imageId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to handle GCS event", e);
        }
    }

    @Override
    public APIResource<SignedUrlResponse> getSignedUrls(SignedUrlRequest request) {
        log.info("Generating signed URLs for request: {}", request);
        Map<String, String> signedUrls = gcsService.generateUploadUrls(request.getObjectPathsWithContentType());
        log.info("Generated {} signed URLs", signedUrls.size());
        return APIResource.success(new SignedUrlResponse(signedUrls));
    }

    @Override
    public String getSignedUrl(String path) {
        return gcsService.generateDownloadUrl(path).toString();
    }

    private String resolveTopicByPrefix(String objectPath) {
        if (objectPath.startsWith("prl_img/")) return "image.uploaded.profile";
        if (objectPath.startsWith("post_img/")) return "image.uploaded.post";
        return "image.uploaded";
    }
}
