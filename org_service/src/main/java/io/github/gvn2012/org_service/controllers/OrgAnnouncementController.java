package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgAnnouncementRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgAnnouncementResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgAnnouncementDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrgAnnouncementResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrgAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/announcements")
@RequiredArgsConstructor
public class OrgAnnouncementController {

    private final IOrgAnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<CreateOrgAnnouncementResponse> createAnnouncement(
            @PathVariable("oid") UUID orgId,
            @RequestHeader("X-User-Id") UUID authorId,
            @Valid @RequestBody CreateOrgAnnouncementRequest request) {
        OrgAnnouncementDto dto = announcementService.createAnnouncement(orgId, authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgAnnouncementResponse.builder()
                        .message("Announcement created successfully")
                        .announcement(dto)
                        .build()
        );
    }

    @PutMapping("/{annid}")
    public ResponseEntity<UpdateOrgAnnouncementResponse> updateAnnouncement(
            @PathVariable("oid") UUID orgId,
            @PathVariable("annid") UUID announcementId,
            @Valid @RequestBody UpdateOrgAnnouncementRequest request) {
        OrgAnnouncementDto dto = announcementService.updateAnnouncement(orgId, announcementId, request);
        return ResponseEntity.ok(
                UpdateOrgAnnouncementResponse.builder()
                        .message("Announcement updated successfully")
                        .announcement(dto)
                        .build()
        );
    }

    @GetMapping("/{annid}")
    public ResponseEntity<OrgAnnouncementDto> getAnnouncementById(
            @PathVariable("oid") UUID orgId,
            @PathVariable("annid") UUID announcementId) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(orgId, announcementId));
    }

    @GetMapping
    public ResponseEntity<Page<OrgAnnouncementDto>> getAnnouncementsByOrgId(
            @PathVariable("oid") UUID orgId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false, defaultValue = "false") boolean pinnedOnly,
            @PageableDefault(size = 20) Pageable pageable) {

        if (pinnedOnly) {
            return ResponseEntity.ok(announcementService.getPinnedAnnouncements(orgId, pageable));
        }

        if (departmentId != null) {
            return ResponseEntity.ok(announcementService.getAnnouncementsByOrgIdAndDepartmentId(orgId, departmentId, pageable));
        }

        return ResponseEntity.ok(announcementService.getAnnouncementsByOrgId(orgId, pageable));
    }

    @DeleteMapping("/{annid}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable("oid") UUID orgId,
            @PathVariable("annid") UUID announcementId) {
        announcementService.deleteAnnouncement(orgId, announcementId);
        return ResponseEntity.noContent().build();
    }
}
