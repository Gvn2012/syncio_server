package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.APIResource;
import io.github.gvn2012.org_service.dtos.requests.CreateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrganizationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgAvailabilityResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrganizationResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final IOrganizationService organizationService;

    @PostMapping
    public ResponseEntity<APIResource<CreateOrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {

        UUID requestingUserId = UUID.fromString(request.getOwnerId());
        CreateOrganizationResponse response = organizationService.createOrganization(requestingUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.ok("Organization created successfully", response));
    }

    @GetMapping("/availability")
    public ResponseEntity<APIResource<OrgAvailabilityResponse>> checkOrgAvailability(
            @RequestParam String name) {
        OrgAvailabilityResponse response = organizationService.getOrgAvailability(name);
        return ResponseEntity.ok(APIResource.ok("Organization availability check successfully", response));
    }

    @GetMapping("/{oid}")
    public ResponseEntity<APIResource<OrganizationDto>> getOrganization(
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId,
            @PathVariable("oid") UUID orgId) {

        OrganizationDto response = organizationService.getOrganization(orgId);
        return ResponseEntity.ok(APIResource.ok("Organization retrieved successfully", response));
    }

    @PutMapping("/{oid}")
    public ResponseEntity<APIResource<UpdateOrganizationResponse>> updateOrganization(
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @PathVariable("oid") UUID orgId,
            @Valid @RequestBody UpdateOrganizationRequest request) {

        UpdateOrganizationResponse response = organizationService.updateOrganization(orgId, requestingUserId, request);
        return ResponseEntity.ok(APIResource.ok("Organization updated successfully", response));
    }

    @DeleteMapping("/{oid}")
    public ResponseEntity<APIResource<Void>> deleteOrganization(
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @PathVariable("oid") UUID orgId) {

        organizationService.deleteOrganization(orgId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Organization deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<APIResource<List<OrganizationDto>>> getOrgsByOwner(
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId,
            @RequestParam(name = "ownerId") UUID ownerId) {

        List<OrganizationDto> response = organizationService.getOrgsByOwner(ownerId);
        return ResponseEntity.ok(APIResource.ok("Organizations retrieved successfully", response));
    }
}
