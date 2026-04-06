package io.github.gvn2012.image_uploading_service.services.interfaces;

import java.net.URL;

public interface GCSServiceInterface {
    URL generateUploadUrl(String objectName, String contentType);
}