package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.clients.UploadClient;
import io.github.gvn2012.org_service.dtos.requests.DownloadUrlRequestDTO;
import io.github.gvn2012.org_service.dtos.responses.DownloadUrlResponseDTO;
import io.github.gvn2012.org_service.dtos.responses.OrganizationDto;
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

    public void enrichOrganizationUrls(Collection<OrganizationDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        Set<String> pathsToSign = dtos.stream()
                .map(OrganizationDto::getLogoPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (pathsToSign.isEmpty()) return;

        try {
            DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();

            for (OrganizationDto dto : dtos) {
                if (dto.getLogoPath() != null) {
                    dto.setLogoUrl(signedUrls.getOrDefault(dto.getLogoPath(), dto.getLogoUrl()));
                }
            }
        } catch (Exception e) {
            log.error("Failed to enrich organization urls: {}", e.getMessage());
        }
    }
}
