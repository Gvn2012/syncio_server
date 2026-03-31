package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgAnnouncementDto;
import io.github.gvn2012.org_service.entities.OrgAnnouncement;

public class OrgAnnouncementMapper {

    public static OrgAnnouncement toEntity(CreateOrgAnnouncementRequest request, java.util.UUID authorId) {
        OrgAnnouncement entity = new OrgAnnouncement();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setAuthorId(authorId);
        entity.setPriority(request.getPriority());
        entity.setPinned(request.getPinned() != null ? request.getPinned() : false);
        entity.setPublishedAt(request.getPublishedAt());
        entity.setExpiresAt(request.getExpiresAt());
        return entity;
    }

    public static OrgAnnouncementDto toDto(OrgAnnouncement entity) {
        return OrgAnnouncementDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .authorId(entity.getAuthorId())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .priority(entity.getPriority())
                .pinned(entity.getPinned())
                .publishedAt(entity.getPublishedAt())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
