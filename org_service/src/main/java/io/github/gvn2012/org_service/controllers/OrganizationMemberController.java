package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.APIResource;
import io.github.gvn2012.org_service.dtos.requests.AddMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateMemberRoleResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrganizationMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/members")
@RequiredArgsConstructor
public class OrganizationMemberController {

    private final IOrganizationMemberService organizationMemberService;

    @PostMapping
    public ResponseEntity<APIResource<AddMemberResponse>> addMember(
            @PathVariable UUID orgId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody AddMemberRequest request) {

        AddMemberResponse response = organizationMemberService.addMember(orgId, requestingUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.ok("Member added successfully", response));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<APIResource<OrganizationMemberDto>> getMember(
            @PathVariable UUID orgId,
            @PathVariable UUID memberId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        OrganizationMemberDto response = organizationMemberService.getMember(orgId, memberId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Member retrieved successfully", response));
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<APIResource<UpdateMemberRoleResponse>> updateMemberRole(
            @PathVariable UUID orgId,
            @PathVariable UUID memberId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {

        UpdateMemberRoleResponse response = organizationMemberService.updateMemberRole(orgId, memberId, requestingUserId, request);
        return ResponseEntity.ok(APIResource.ok("Member role updated successfully", response));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<APIResource<Void>> removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID memberId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {

        organizationMemberService.removeMember(orgId, memberId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Member removed successfully", null));
    }

    @GetMapping
    public ResponseEntity<APIResource<List<OrganizationMemberDto>>> getMembers(
            @PathVariable UUID orgId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        List<OrganizationMemberDto> response = organizationMemberService.getMembers(orgId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Members retrieved successfully", response));
    }
}
