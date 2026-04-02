package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgJobLevelResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgJobLevelDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrgJobLevelResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrgJobLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/job-levels")
@RequiredArgsConstructor
public class OrgJobLevelController {

    private final IOrgJobLevelService jobLevelService;

    @PostMapping
    public ResponseEntity<CreateOrgJobLevelResponse> createJobLevel(
            @PathVariable("oid") UUID orgId,
            @Valid @RequestBody CreateOrgJobLevelRequest request) {
        OrgJobLevelDto dto = jobLevelService.createJobLevel(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgJobLevelResponse.builder()
                        .message("Job level created successfully")
                        .jobLevel(dto)
                        .build()
        );
    }

    @PutMapping("/{jlid}")
    public ResponseEntity<UpdateOrgJobLevelResponse> updateJobLevel(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jlid") UUID jobLevelId,
            @Valid @RequestBody UpdateOrgJobLevelRequest request) {
        OrgJobLevelDto dto = jobLevelService.updateJobLevel(orgId, jobLevelId, request);
        return ResponseEntity.ok(
                UpdateOrgJobLevelResponse.builder()
                        .message("Job level updated successfully")
                        .jobLevel(dto)
                        .build()
        );
    }

    @GetMapping("/{jlid}")
    public ResponseEntity<OrgJobLevelDto> getJobLevelById(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jlid") UUID jobLevelId) {
        return ResponseEntity.ok(jobLevelService.getJobLevelById(orgId, jobLevelId));
    }

    @GetMapping
    public ResponseEntity<List<OrgJobLevelDto>> getJobLevelsByOrgId(
            @PathVariable("oid") UUID orgId) {
        return ResponseEntity.ok(jobLevelService.getJobLevelsByOrgId(orgId));
    }

    @DeleteMapping("/{jlid}")
    public ResponseEntity<Void> deleteJobLevel(
            @PathVariable("oid") UUID orgId,
            @PathVariable("jlid") UUID jobLevelId) {
        jobLevelService.deleteJobLevel(orgId, jobLevelId);
        return ResponseEntity.noContent().build();
    }
}
