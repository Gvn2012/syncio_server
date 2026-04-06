package io.github.gvn2012.image_uploading_service.services.interfaces;

import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;

import java.util.Map;

public interface UploadServiceInterface{
    APIResource<UploadResponse> sendUploadRequest(UploadRequest uploadRequest);
    APIResource<UploadConfirmResponse> confirmUpload(UploadConfirmRequest request);
    void handle(Map<String, Object> body);
}

