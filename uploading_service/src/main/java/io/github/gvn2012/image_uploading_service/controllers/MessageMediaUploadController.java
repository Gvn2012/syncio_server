package io.github.gvn2012.image_uploading_service.controllers;

import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadBatchRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadBatchResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.services.interfaces.MessageMediaUploadServiceInterface;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/upload/messages")
@RequiredArgsConstructor
public class MessageMediaUploadController {

    private final MessageMediaUploadServiceInterface uploadService;

    @PostMapping
    public ResponseEntity<APIResource<UploadResponse>> sendUploadRequest(
            @RequestBody MessageMediaUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadService.sendUploadRequest(request));
    }

    @PostMapping("/batch")
    public ResponseEntity<APIResource<UploadBatchResponse>> sendBatchUploadRequest(
            @RequestBody MessageMediaUploadBatchRequest request) {
        if (request.getRequests() != null && request.getRequests().size() > 25) {
            throw new IllegalArgumentException("Maximum 25 items allowed per batch upload");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadService.sendBatchUploadRequest(request));
    }
}
