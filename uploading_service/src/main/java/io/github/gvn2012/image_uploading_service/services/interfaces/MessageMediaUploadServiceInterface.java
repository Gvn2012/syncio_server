package io.github.gvn2012.image_uploading_service.services.interfaces;

import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadBatchRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.MessageMediaUploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadBatchResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;
import io.github.gvn2012.image_uploading_service.dtos.APIResource;

public interface MessageMediaUploadServiceInterface {
    APIResource<UploadResponse> sendUploadRequest(MessageMediaUploadRequest request);
    APIResource<UploadBatchResponse> sendBatchUploadRequest(MessageMediaUploadBatchRequest request);
}
