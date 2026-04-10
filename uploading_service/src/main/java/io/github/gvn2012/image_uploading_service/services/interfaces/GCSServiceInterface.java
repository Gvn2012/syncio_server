package io.github.gvn2012.image_uploading_service.services.interfaces;

import java.net.URL;
import java.util.Map;
import java.util.Set;

public interface GCSServiceInterface {
    URL generateUploadUrl(String objectName, String contentType);

    String generateDownloadUrl(String objectPath);

    Map<String, String> generateDownloadUrls(Set<String> objectPaths);
}