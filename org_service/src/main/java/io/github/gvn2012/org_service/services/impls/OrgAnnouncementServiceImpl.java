package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgAnnouncementMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgAnnouncementDto;
import io.github.gvn2012.org_service.entities.Department;
import io.github.gvn2012.org_service.entities.OrgAnnouncement;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.repositories.DepartmentRepository;
import io.github.gvn2012.org_service.repositories.OrgAnnouncementRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgAnnouncementService;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgAnnouncementServiceImpl implements IOrgAnnouncementService {

    private final OrgAnnouncementRepository announcementRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public OrgAnnouncementDto createAnnouncement(UUID orgId, UUID authorId, CreateOrgAnnouncementRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        OrgAnnouncement announcement = OrgAnnouncementMapper.toEntity(request, authorId);
        announcement.setOrganization(organization);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            announcement.setDepartment(department);
        }

        OrgAnnouncement savedAnnouncement = announcementRepository.save(announcement);
        return OrgAnnouncementMapper.toDto(savedAnnouncement);
    }

    @Override
    @Transactional
    public OrgAnnouncementDto updateAnnouncement(UUID orgId, UUID announcementId, UpdateOrgAnnouncementRequest request) {
        OrgAnnouncement announcement = announcementRepository.findByIdAndOrganization_Id(announcementId, orgId)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            announcement.setTitle(request.getTitle());
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            announcement.setContent(request.getContent());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            announcement.setDepartment(department);
        }

        if (request.getPriority() != null) {
            announcement.setPriority(request.getPriority());
        }

        if (request.getPinned() != null) {
            announcement.setPinned(request.getPinned());
        }

        if (request.getPublishedAt() != null) {
            announcement.setPublishedAt(request.getPublishedAt());
        }

        if (request.getExpiresAt() != null) {
            announcement.setExpiresAt(request.getExpiresAt());
        }

        OrgAnnouncement updatedAnnouncement = announcementRepository.save(announcement);
        return OrgAnnouncementMapper.toDto(updatedAnnouncement);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgAnnouncementDto getAnnouncementById(UUID orgId, UUID announcementId) {
        OrgAnnouncement announcement = announcementRepository.findByIdAndOrganization_Id(announcementId, orgId)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));
        return OrgAnnouncementMapper.toDto(announcement);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgAnnouncementDto> getAnnouncementsByOrgId(UUID orgId, Pageable pageable) {
        return announcementRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId, pageable)
                .map(OrgAnnouncementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgAnnouncementDto> getAnnouncementsByOrgIdAndDepartmentId(UUID orgId, UUID departmentId, Pageable pageable) {
        return announcementRepository.findByOrganization_IdAndDepartment_IdOrderByCreatedAtDesc(orgId, departmentId, pageable)
                .map(OrgAnnouncementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgAnnouncementDto> getPinnedAnnouncements(UUID orgId, Pageable pageable) {
        return announcementRepository.findByOrganization_IdAndPinnedTrueOrderByCreatedAtDesc(orgId, pageable)
                .map(OrgAnnouncementMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(UUID orgId, UUID announcementId) {
        OrgAnnouncement announcement = announcementRepository.findByIdAndOrganization_Id(announcementId, orgId)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));
        announcementRepository.delete(announcement);
    }
}
