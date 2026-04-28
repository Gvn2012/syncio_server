package io.github.gvn2012.image_uploading_service.services.impls;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GCSServiceImpl implements GCSServiceInterface {

    private static final long SIGN_TTL_MINUTES = 60;
    private static final Duration CACHE_TTL = Duration.ofMinutes(55);
    private static final String CACHE_PREFIX = "signed_url:";

    private final StringRedisTemplate redisTemplate;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    @Value("${gcp.storage.service-account:}")
    private String serviceAccountEmail;

    private Storage storage;

    @PostConstruct
    public void init() throws IOException {
        log.info("Initializing GCS service with bucket: {} and service account: {}", bucket, serviceAccountEmail);
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    @Override
    public URL generateUploadUrl(String objectName, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectName)
                .setContentType(contentType)
                .build();

        return storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature());
    }

    @Override
    public URL generateResumableUploadUrl(String objectName, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectName)
                .setContentType(contentType)
                .build();

        return storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.POST),
                Storage.SignUrlOption.withExtHeaders(Map.of("x-goog-resumable", "start")),
                Storage.SignUrlOption.withV4Signature());
    }

    @Override
    public String generateDownloadUrl(String objectPath) {
        String cacheKey = CACHE_PREFIX + objectPath;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String signedUrl = signForGet(objectPath);
        redisTemplate.opsForValue().set(cacheKey, signedUrl, CACHE_TTL);
        return signedUrl;
    }

    @Override
    public Map<String, String> generateDownloadUrls(Set<String> objectPaths) {
        Map<String, String> result = new HashMap<>(objectPaths.size());

        List<String> keys = objectPaths.stream()
                .filter(p -> p != null && !p.isBlank())
                .map(p -> CACHE_PREFIX + p)
                .toList();

        List<String> cached = redisTemplate.opsForValue().multiGet(keys);

        int i = 0;
        for (String objectPath : objectPaths) {
            if (objectPath == null || objectPath.isBlank())
                continue;

            String cachedUrl = (cached != null && i < cached.size()) ? cached.get(i) : null;
            if (cachedUrl != null) {
                result.put(objectPath, cachedUrl);
            } else {
                String signedUrl = signForGet(objectPath);
                redisTemplate.opsForValue().set(CACHE_PREFIX + objectPath, signedUrl, CACHE_TTL);
                result.put(objectPath, signedUrl);
            }
            i++;
        }

        return result;
    }

    private String signForGet(String objectPath) {
        try {
            log.info("Attempting to sign URL for object: {}", objectPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectPath).build();
            Storage.SignUrlOption[] options = {
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature()
            };

            if (serviceAccountEmail != null && !serviceAccountEmail.isBlank()) {
                log.debug("Using service account email for signing: {}", serviceAccountEmail);
            }

            URL url = storage.signUrl(
                    blobInfo,
                    SIGN_TTL_MINUTES, TimeUnit.MINUTES,
                    options);
            String signedUrl = url.toString();
            if (!signedUrl.contains("X-Goog-Signature")) {
                log.warn("Generated URL for {} does not contain a signature: {}", objectPath, signedUrl);
            } else {
                log.info("Successfully signed URL for {}", objectPath);
            }
            return signedUrl;
        } catch (Exception e) {
            log.error("Error signing URL for object {}: {}", objectPath, e.getMessage(), e);
            return String.format("https://storage.googleapis.com/%s/%s", bucket, objectPath);
        }
    }

    @Override
    public Map<String, String> generateUploadUrls(Map<String, String> pathContentTypes) {
        if (pathContentTypes == null) {
            log.warn("generateUploadUrls called with null pathContentTypes");
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(pathContentTypes.size());
        for (Map.Entry<String, String> entry : pathContentTypes.entrySet()) {
            String objectPath = entry.getKey();
            String contentType = entry.getValue();

            BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectPath)
                    .setContentType(contentType)
                    .build();

            URL url = storage.signUrl(
                    blobInfo,
                    15, TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                    Storage.SignUrlOption.withV4Signature());
            result.put(objectPath, url.toString());
        }
        return result;
    }
}