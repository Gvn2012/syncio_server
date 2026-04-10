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
    private static final Duration CACHE_TTL = Duration.ofMinutes(50);
    private static final String CACHE_PREFIX = "signed_url:";

    private final StringRedisTemplate redisTemplate;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    private Storage storage;

    @PostConstruct
    public void init() throws IOException {
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
                Storage.SignUrlOption.withV4Signature()
        );
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
            if (objectPath == null || objectPath.isBlank()) continue;

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
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectPath).build();
        URL url = storage.signUrl(
                blobInfo,
                SIGN_TTL_MINUTES, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature()
        );
        return url.toString();
    }
}