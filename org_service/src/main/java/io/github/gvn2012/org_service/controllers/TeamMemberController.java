package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.APIResource;
import io.github.gvn2012.org_service.dtos.requests.AddTeamMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddTeamMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamMemberRoleResponse;
import io.github.gvn2012.org_service.services.interfaces.ITeamMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/departments/{did}/teams/{tid}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final ITeamMemberService teamMemberService;

    @PostMapping
    public ResponseEntity<APIResource<AddTeamMemberResponse>> addTeamMember(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @PathVariable("tid") UUID teamId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody AddTeamMemberRequest request) {

        AddTeamMemberResponse response = teamMemberService.addTeamMember(orgId, deptId, teamId, requestingUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.ok("Team member added successfully", response));
    }

    @GetMapping("/{mid}")
    public ResponseEntity<APIResource<TeamMemberDto>> getTeamMember(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @PathVariable("tid") UUID teamId,
            @PathVariable("mid") UUID memberId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        TeamMemberDto response = teamMemberService.getTeamMember(orgId, deptId, teamId, memberId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Team member retrieved successfully", response));
    }

    @PutMapping("/{mid}/role")
    public ResponseEntity<APIResource<UpdateTeamMemberRoleResponse>> updateTeamMemberRole(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @PathVariable("tid") UUID teamId,
            @PathVariable("mid") UUID memberId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody UpdateTeamMemberRoleRequest request) {

        UpdateTeamMemberRoleResponse response = teamMemberService.updateTeamMemberRole(orgId, deptId, teamId, memberId, requestingUserId, request);
        return ResponseEntity.ok(APIResource.ok("Team member role updated successfully", response));
    }

    @DeleteMapping("/{mid}")
    public ResponseEntity<APIResource<Void>> removeTeamMember(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @PathVariable("tid") UUID teamId,
            @PathVariable("mid") UUID memberId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {

        teamMemberService.removeTeamMember(orgId, deptId, teamId, memberId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Team member removed successfully", null));
    }

    @GetMapping
    public ResponseEntity<APIResource<List<TeamMemberDto>>> getTeamMembers(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @PathVariable("tid") UUID teamId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        List<TeamMemberDto> response = teamMemberService.getTeamMembers(orgId, deptId, teamId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Team members retrieved successfully", response));
    }
}
