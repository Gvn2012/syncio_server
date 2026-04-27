package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.clients.UploadClient;
import io.github.gvn2012.user_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.user_service.dtos.responses.DownloadUrlResponseDTO;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.UserProfilePictureResponse;
import io.github.gvn2012.user_service.dtos.responses.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEnrichmentService {

    private final UploadClient uploadClient;

    public void enrichUserDetailMediaUrls(List<GetUserDetailResponse> responses) {
        if (responses == null || responses.isEmpty()) return;

        Set<String> pathsToSign = new HashSet<>();
        for (GetUserDetailResponse res : responses) {
            if (res.getUserProfileResponse() != null && res.getUserProfileResponse().getUserProfilePictureResponseList() != null) {
                res.getUserProfileResponse().getUserProfilePictureResponseList().stream()
                        .map(UserProfilePictureResponse::getObjectPath)
                        .filter(Objects::nonNull)
                        .forEach(pathsToSign::add);
            }
        }

        if (pathsToSign.isEmpty()) return;

        try {
            DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Collections.emptyMap();

            for (GetUserDetailResponse res : responses) {
                if (res.getUserProfileResponse() != null && res.getUserProfileResponse().getUserProfilePictureResponseList() != null) {
                    for (UserProfilePictureResponse pic : res.getUserProfileResponse().getUserProfilePictureResponseList()) {
                        if (pic.getObjectPath() != null) {
                            pic.setUrl(signedUrls.getOrDefault(pic.getObjectPath(), pic.getUrl()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich user detail media URLs: {}", e.getMessage());
        }
    }

    public void enrichUserSummaryMediaUrls(Collection<UserSummaryResponse> summaries) {
        if (summaries == null || summaries.isEmpty()) return;

        Set<String> pathsToSign = new HashSet<>();
        for (UserSummaryResponse summary : summaries) {
            if (summary.getAvatarPath() != null) {
                pathsToSign.add(summary.getAvatarPath());
            }
        }

        if (pathsToSign.isEmpty()) return;

        try {
            DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Collections.emptyMap();

            for (UserSummaryResponse summary : summaries) {
                if (summary.getAvatarPath() != null) {
                    summary.setAvatarUrl(signedUrls.getOrDefault(summary.getAvatarPath(), summary.getAvatarUrl()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich user summary media URLs: {}", e.getMessage());
        }
    }
}
