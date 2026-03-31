package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgJobTitleResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgJobTitleDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrgJobTitleResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrgJobTitleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/job-titles")
@RequiredArgsConstructor
public class OrgJobTitleController {

    private final IOrgJobTitleService jobTitleService;

    @PostMapping
    public ResponseEntity<CreateOrgJobTitleResponse> createJobTitle(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateOrgJobTitleRequest request) {
        OrgJobTitleDto dto = jobTitleService.createJobTitle(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgJobTitleResponse.builder()
                        .message("Job title created successfully")
                        .jobTitle(dto)
                        .build()
        );
    }

    @PutMapping("/{jobTitleId}")
    public ResponseEntity<UpdateOrgJobTitleResponse> updateJobTitle(
            @PathVariable UUID orgId,
            @PathVariable UUID jobTitleId,
            @Valid @RequestBody UpdateOrgJobTitleRequest request) {
        OrgJobTitleDto dto = jobTitleService.updateJobTitle(orgId, jobTitleId, request);
        return ResponseEntity.ok(
                UpdateOrgJobTitleResponse.builder()
                        .message("Job title updated successfully")
                        .jobTitle(dto)
                        .build()
        );
    }

    @GetMapping("/{jobTitleId}")
    public ResponseEntity<OrgJobTitleDto> getJobTitleById(
            @PathVariable UUID orgId,
            @PathVariable UUID jobTitleId) {
        return ResponseEntity.ok(jobTitleService.getJobTitleById(orgId, jobTitleId));
    }

    @GetMapping
    public ResponseEntity<List<OrgJobTitleDto>> getJobTitlesByOrgId(
            @PathVariable UUID orgId) {
        return ResponseEntity.ok(jobTitleService.getJobTitlesByOrgId(orgId));
    }

    @DeleteMapping("/{jobTitleId}")
    public ResponseEntity<Void> deleteJobTitle(
            @PathVariable UUID orgId,
            @PathVariable UUID jobTitleId) {
        jobTitleService.deleteJobTitle(orgId, jobTitleId);
        return ResponseEntity.noContent().build();
    }
}
