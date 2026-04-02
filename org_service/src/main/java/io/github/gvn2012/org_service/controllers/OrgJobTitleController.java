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
@RequestMapping("/api/v1/orgs/{oid}/job-titles")
@RequiredArgsConstructor
public class OrgJobTitleController {

    private final IOrgJobTitleService jobTitleService;

    @PostMapping
    public ResponseEntity<CreateOrgJobTitleResponse> createJobTitle(
            @PathVariable("oid") UUID orgId,
            @Valid @RequestBody CreateOrgJobTitleRequest request) {
        OrgJobTitleDto dto = jobTitleService.createJobTitle(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgJobTitleResponse.builder()
                        .message("Job title created successfully")
                        .jobTitle(dto)
                        .build()
        );
    }

    @PutMapping("/{jtid}")
    public ResponseEntity<UpdateOrgJobTitleResponse> updateJobTitle(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jtid") UUID jobTitleId,
            @Valid @RequestBody UpdateOrgJobTitleRequest request) {
        OrgJobTitleDto dto = jobTitleService.updateJobTitle(orgId, jobTitleId, request);
        return ResponseEntity.ok(
                UpdateOrgJobTitleResponse.builder()
                        .message("Job title updated successfully")
                        .jobTitle(dto)
                        .build()
        );
    }

    @GetMapping("/{jtid}")
    public ResponseEntity<OrgJobTitleDto> getJobTitleById(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jtid") UUID jobTitleId) {
        return ResponseEntity.ok(jobTitleService.getJobTitleById(orgId, jobTitleId));
    }

    @GetMapping
    public ResponseEntity<List<OrgJobTitleDto>> getJobTitlesByOrgId(
            @PathVariable("oid") UUID orgId) {
        return ResponseEntity.ok(jobTitleService.getJobTitlesByOrgId(orgId));
    }

    @DeleteMapping("/{jtid}")
    public ResponseEntity<Void> deleteJobTitle(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jtid") UUID jobTitleId) {
        jobTitleService.deleteJobTitle(orgId, jobTitleId);
        return ResponseEntity.noContent().build();
    }
}
