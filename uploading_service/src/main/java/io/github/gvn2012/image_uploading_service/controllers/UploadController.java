package io.github.gvn2012.image_uploading_service.controllers;

import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.SignedUrlRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.SignedUrlResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.services.interfaces.UploadServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadServiceInterface uploadService;

    @PostMapping
    public ResponseEntity<APIResource<UploadResponse>> sendUploadRequest(
            @RequestBody UploadRequest uploadRequest){

        APIResource<UploadResponse> uploadResponse =
                uploadService.sendUploadRequest(uploadRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadResponse);
    }

    @PostMapping("/confirm")
    public ResponseEntity<APIResource<UploadConfirmResponse>> confirmUpload(
            @RequestBody UploadConfirmRequest uploadConfirmRequest){

        APIResource<UploadConfirmResponse> response =
                uploadService.confirmUpload(uploadConfirmRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/gcs-events")
    public ResponseEntity<Void> handle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        log.info("Received GCS event: {}", body);
        try {
            uploadService.handle(body);
        } catch (Exception e) {
            log.error("Error handling GCS event", e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/signed-urls")
    public ResponseEntity<APIResource<SignedUrlResponse>> getSignedUrls(
            @RequestBody SignedUrlRequest request) {
        return ResponseEntity.ok(uploadService.getSignedUrls(request));
    }

    @GetMapping("/view")
    public ResponseEntity<Void> view(@RequestParam String path) {
        String signedUrl = uploadService.getSignedUrl(path);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", signedUrl)
                .build();
    }
}
