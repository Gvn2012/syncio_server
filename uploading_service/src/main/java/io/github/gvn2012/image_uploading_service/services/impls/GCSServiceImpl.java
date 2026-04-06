package io.github.gvn2012.image_uploading_service.services.impls;

import com.google.cloud.storage.*;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GCSServiceImpl implements GCSServiceInterface {

    @Value("${gcp.storage.bucket}")
    private String bucket;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    @Override
    public URL generateUploadUrl(String objectName, String contentType) {

        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectName)
                .setContentType(contentType)
                .build();

        return storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );
    }
}