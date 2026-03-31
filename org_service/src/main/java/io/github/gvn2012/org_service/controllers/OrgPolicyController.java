package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgPolicyResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgPolicyDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrgPolicyResponse;
import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import io.github.gvn2012.org_service.services.interfaces.IOrgPolicyService;
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
@RequestMapping("/api/v1/organizations/{orgId}/policies")
@RequiredArgsConstructor
public class OrgPolicyController {

    private final IOrgPolicyService policyService;

    @PostMapping
    public ResponseEntity<CreateOrgPolicyResponse> createPolicy(
            @PathVariable UUID orgId,
            @RequestHeader(value = "X-User-Id", required = false) UUID approvedById,
            @Valid @RequestBody CreateOrgPolicyRequest request) {
        OrgPolicyDto dto = policyService.createPolicy(orgId, approvedById, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgPolicyResponse.builder()
                        .message("Policy created successfully")
                        .policy(dto)
                        .build()
        );
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<UpdateOrgPolicyResponse> updatePolicy(
            @PathVariable UUID orgId,
            @PathVariable UUID policyId,
            @Valid @RequestBody UpdateOrgPolicyRequest request) {
        OrgPolicyDto dto = policyService.updatePolicy(orgId, policyId, request);
        return ResponseEntity.ok(
                UpdateOrgPolicyResponse.builder()
                        .message("Policy updated successfully")
                        .policy(dto)
                        .build()
        );
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<OrgPolicyDto> getPolicyById(
            @PathVariable UUID orgId,
            @PathVariable UUID policyId) {
        return ResponseEntity.ok(policyService.getPolicyById(orgId, policyId));
    }

    @GetMapping
    public ResponseEntity<Page<OrgPolicyDto>> getPoliciesByOrgId(
            @PathVariable UUID orgId,
            @RequestParam(required = false) PolicyCategory category,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {

        if (activeOnly) {
            return ResponseEntity.ok(policyService.getActivePoliciesByOrgId(orgId, pageable));
        }

        if (category != null) {
            return ResponseEntity.ok(policyService.getPoliciesByOrgIdAndCategory(orgId, category, pageable));
        }

        return ResponseEntity.ok(policyService.getPoliciesByOrgId(orgId, pageable));
    }

    @DeleteMapping("/{policyId}")
    public ResponseEntity<Void> deletePolicy(
            @PathVariable UUID orgId,
            @PathVariable UUID policyId) {
        policyService.deletePolicy(orgId, policyId);
        return ResponseEntity.noContent().build();
    }
}
