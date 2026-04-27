package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.clients.UploadClient;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipUserSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaEnrichmentService {

    private final UploadClient uploadClient;

    public void enrichRelationshipUserSummaries(Collection<RelationshipUserSummaryResponse> summaries) {
        if (summaries == null || summaries.isEmpty()) return;

        Set<String> pathsToSign = new HashSet<>();
        for (RelationshipUserSummaryResponse summary : summaries) {
            if (summary.getProfilePicturePath() != null) {
                pathsToSign.add(summary.getProfilePicturePath());
            }
        }

        if (pathsToSign.isEmpty()) return;

        try {
            Map<String, String> signedUrls = uploadClient.getDownloadUrls(pathsToSign);

            for (RelationshipUserSummaryResponse summary : summaries) {
                if (summary.getProfilePicturePath() != null) {
                    summary.setProfilePictureUrl(signedUrls.getOrDefault(summary.getProfilePicturePath(), summary.getProfilePictureUrl()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich relationship user summaries: {}", e.getMessage());
        }
    }
}
