package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgAnnouncementDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IOrgAnnouncementService {
    OrgAnnouncementDto createAnnouncement(UUID orgId, UUID authorId, CreateOrgAnnouncementRequest request);
    OrgAnnouncementDto updateAnnouncement(UUID orgId, UUID announcementId, UpdateOrgAnnouncementRequest request);
    OrgAnnouncementDto getAnnouncementById(UUID orgId, UUID announcementId);
    Page<OrgAnnouncementDto> getAnnouncementsByOrgId(UUID orgId, Pageable pageable);
    Page<OrgAnnouncementDto> getAnnouncementsByOrgIdAndDepartmentId(UUID orgId, UUID departmentId, Pageable pageable);
    Page<OrgAnnouncementDto> getPinnedAnnouncements(UUID orgId, Pageable pageable);
    void deleteAnnouncement(UUID orgId, UUID announcementId);
}
