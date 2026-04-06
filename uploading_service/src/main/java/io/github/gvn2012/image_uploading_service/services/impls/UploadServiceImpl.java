package io.github.gvn2012.image_uploading_service.services.impls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.models.UploadAudit;
import io.github.gvn2012.image_uploading_service.repositories.UploadAuditRepository;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import io.github.gvn2012.image_uploading_service.services.interfaces.UploadServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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
        String objectPath = "uploads/" + imageId + "-" + request.getFileName();

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
                900
        );

        return APIResource.success(response);
    }

    @Override
    public APIResource<UploadConfirmResponse> confirmUpload(UploadConfirmRequest request) {
        UploadAudit audit = uploadAuditRepository.findByImageId(request.getImageId())
                .orElseThrow(() -> new RuntimeException("Upload not found"));

        UploadConfirmResponse response = new UploadConfirmResponse(
                audit.getImageId(),
                audit.getStatus()
        );

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

            String imageId = objectName.split("/")[1].split("-")[0];

            uploadAuditRepository.findByImageId(imageId).ifPresent(audit -> {
                audit.setStatus("COMPLETED");
                audit.setUpdatedAt(Instant.now());
                uploadAuditRepository.save(audit);
            });

            kafkaTemplate.send("image.uploaded", imageId, json);

        } catch (Exception e) {
            throw new RuntimeException("Failed to handle GCS event", e);
        }
    }
}
