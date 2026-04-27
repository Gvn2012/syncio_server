package io.github.gvn2012.image_uploading_service.services.interfaces;

import io.github.gvn2012.image_uploading_service.dtos.APIResource;
import io.github.gvn2012.image_uploading_service.dtos.requests.SignedUrlRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadConfirmRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.UploadRequest;
import io.github.gvn2012.image_uploading_service.dtos.requests.DownloadUrlRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.DownloadUrlResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.SignedUrlResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadConfirmResponse;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadResponse;

import io.github.gvn2012.image_uploading_service.dtos.requests.UploadBatchRequest;
import io.github.gvn2012.image_uploading_service.dtos.responses.UploadBatchResponse;

import java.util.Map;

public interface UploadServiceInterface{
    APIResource<UploadResponse> sendUploadRequest(UploadRequest uploadRequest);
    APIResource<UploadBatchResponse> sendBatchUploadRequest(UploadBatchRequest request);
    APIResource<UploadConfirmResponse> confirmUpload(UploadConfirmRequest request);
    void handle(Map<String, Object> body);
    APIResource<SignedUrlResponse> getSignedUrls(SignedUrlRequest request);
    APIResource<DownloadUrlResponse> getDownloadUrls(DownloadUrlRequest request);
    String getSignedUrl(String path);
}
