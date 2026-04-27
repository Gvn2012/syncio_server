package io.github.gvn2012.search_service.services.impls;

import io.github.gvn2012.search_service.clients.UploadClient;
import io.github.gvn2012.search_service.documents.UserIndex;
import io.github.gvn2012.search_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.search_service.dtos.responses.DownloadUrlResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEnrichmentService {

    private final UploadClient uploadClient;

    public void enrichUserMediaUrls(Collection<UserIndex> users) {
        if (users == null || users.isEmpty()) return;

        Set<String> pathsToSign = users.stream()
                .map(UserIndex::getAvatarPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pathsToSign.isEmpty()) return;

        try {
            DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();

            for (UserIndex user : users) {
                if (user.getAvatarPath() != null) {
                    user.setAvatarUrl(signedUrls.getOrDefault(user.getAvatarPath(), user.getAvatarUrl()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich user media urls: {}", e.getMessage());
        }
    }
}
