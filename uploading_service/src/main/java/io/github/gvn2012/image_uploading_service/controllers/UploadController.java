package io.github.gvn2012.image_uploading_service.controllers;

import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.services.interfaces.UploadServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        try {
            uploadService.handle(body);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}
