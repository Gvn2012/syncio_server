package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgInvitationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgInvitationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgInvitationDto;
import io.github.gvn2012.org_service.entities.enums.InvitationStatus;
import io.github.gvn2012.org_service.services.interfaces.IOrgInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/invitations")
@RequiredArgsConstructor
public class OrgInvitationController {

    private final IOrgInvitationService invitationService;

    @PostMapping
    public ResponseEntity<CreateOrgInvitationResponse> createInvitation(
            @PathVariable UUID orgId,
            @RequestHeader("X-User-Id") UUID invitedByUserId,
            @Valid @RequestBody CreateOrgInvitationRequest request) {
        CreateOrgInvitationResponse response = invitationService.createInvitation(orgId, invitedByUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{invitationId}")
    public ResponseEntity<OrgInvitationDto> getInvitationById(
            @PathVariable UUID orgId,
            @PathVariable UUID invitationId) {
        return ResponseEntity.ok(invitationService.getInvitationById(orgId, invitationId));
    }

    @GetMapping
    public ResponseEntity<List<OrgInvitationDto>> getInvitationsByOrgId(
            @PathVariable UUID orgId,
            @RequestParam(required = false) InvitationStatus status) {
        return ResponseEntity.ok(invitationService.getInvitationsByOrgId(orgId, status));
    }

    @PostMapping("/{invitationId}/cancel")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable UUID orgId,
            @PathVariable UUID invitationId) {
        invitationService.cancelInvitation(orgId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable UUID orgId,
            @PathVariable UUID invitationId,
            @RequestHeader("X-User-Id") UUID acceptedByUserId,
            @RequestParam String token) {
        invitationService.acceptInvitation(orgId, invitationId, acceptedByUserId, token);
        return ResponseEntity.noContent().build();
    }
}
