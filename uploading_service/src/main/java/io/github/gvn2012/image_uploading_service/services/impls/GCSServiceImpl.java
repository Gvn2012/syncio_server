package io.github.gvn2012.image_uploading_service.services.impls;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import io.github.gvn2012.image_uploading_service.services.interfaces.GCSServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GCSServiceImpl implements GCSServiceInterface {

    private static final long SIGN_TTL_MINUTES = 60;
    private static final long CACHE_TTL_MINUTES = 50;

    @Value("${gcp.storage.bucket}")
    private String bucket;

    private Storage storage;
    private final ConcurrentHashMap<String, CachedSignedUrl> downloadUrlCache = new ConcurrentHashMap<>();

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
        CachedSignedUrl cached = downloadUrlCache.get(objectPath);
        if (cached != null && !cached.isExpired()) {
            return cached.url;
        }

        String signedUrl = signForGet(objectPath);
        downloadUrlCache.put(objectPath, new CachedSignedUrl(signedUrl,
                Instant.now().plusSeconds(CACHE_TTL_MINUTES * 60)));
        return signedUrl;
    }

    @Override
    public Map<String, String> generateDownloadUrls(Set<String> objectPaths) {
        Map<String, String> result = new HashMap<>(objectPaths.size());

        for (String objectPath : objectPaths) {
            if (objectPath == null || objectPath.isBlank()) continue;
            result.put(objectPath, generateDownloadUrl(objectPath));
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

    @Scheduled(fixedRate = 300_000)
    public void evictExpiredEntries() {
        int before = downloadUrlCache.size();
        downloadUrlCache.entrySet().removeIf(e -> e.getValue().isExpired());
        int evicted = before - downloadUrlCache.size();
        if (evicted > 0) {
            log.debug("Evicted {} expired signed URL cache entries", evicted);
        }
    }

    private record CachedSignedUrl(String url, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}