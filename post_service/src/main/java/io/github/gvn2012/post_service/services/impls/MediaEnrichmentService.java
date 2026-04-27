package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.UploadClient;
import io.github.gvn2012.post_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.post_service.dtos.responses.DownloadUrlResponseDTO;
import io.github.gvn2012.post_service.dtos.responses.MediaAttachmentResponse;
import io.github.gvn2012.post_service.dtos.responses.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEnrichmentService {

    private final UploadClient uploadClient;

    public void enrichMediaUrls(List<PostResponse> responses) {
        if (responses == null || responses.isEmpty())
            return;

        Set<String> pathsToSign = new HashSet<>();
        for (PostResponse res : responses) {
            if (res.getAttachments() != null) {
                res.getAttachments().stream()
                        .map(MediaAttachmentResponse::getObjectPath)
                        .filter(Objects::nonNull)
                        .forEach(pathsToSign::add);
            }
            if (res.getAuthorInfo() != null && res.getAuthorInfo().getAvatarPath() != null) {
                pathsToSign.add(res.getAuthorInfo().getAvatarPath());
            }
        }

        if (pathsToSign.isEmpty())
            return;

        try {
            DownloadUrlResponseDTO urlRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = urlRes != null ? urlRes.getDownloadUrls() : Collections.emptyMap();

            for (PostResponse res : responses) {
                if (res.getAttachments() != null) {
                    for (MediaAttachmentResponse attachment : res.getAttachments()) {
                        if (attachment.getObjectPath() != null) {
                            attachment.setUrl(signedUrls.getOrDefault(attachment.getObjectPath(), attachment.getUrl()));
                        }
                    }
                }
                if (res.getAuthorInfo() != null && res.getAuthorInfo().getAvatarPath() != null) {
                    res.getAuthorInfo().setAvatarUrl(signedUrls.getOrDefault(res.getAuthorInfo().getAvatarPath(),
                            res.getAuthorInfo().getAvatarUrl()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich media URLs: {}", e.getMessage());
        }
    }
}
