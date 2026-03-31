package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.APIResource;
import io.github.gvn2012.org_service.dtos.requests.CreateTeamRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateTeamResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamResponse;
import io.github.gvn2012.org_service.services.interfaces.ITeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/departments/{deptId}/teams")
@RequiredArgsConstructor
public class TeamController {

    private final ITeamService teamService;

    @PostMapping
    public ResponseEntity<APIResource<CreateTeamResponse>> createTeam(
            @PathVariable UUID orgId,
            @PathVariable UUID deptId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody CreateTeamRequest request) {

        CreateTeamResponse response = teamService.createTeam(orgId, deptId, requestingUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.ok("Team created successfully", response));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<APIResource<TeamDto>> getTeam(
            @PathVariable UUID orgId,
            @PathVariable UUID deptId,
            @PathVariable UUID teamId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        TeamDto response = teamService.getTeam(orgId, deptId, teamId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Team retrieved successfully", response));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<APIResource<UpdateTeamResponse>> updateTeam(
            @PathVariable UUID orgId,
            @PathVariable UUID deptId,
            @PathVariable UUID teamId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody UpdateTeamRequest request) {

        UpdateTeamResponse response = teamService.updateTeam(orgId, deptId, teamId, requestingUserId, request);
        return ResponseEntity.ok(APIResource.ok("Team updated successfully", response));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<APIResource<Void>> deleteTeam(
            @PathVariable UUID orgId,
            @PathVariable UUID deptId,
            @PathVariable UUID teamId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {

        teamService.deleteTeam(orgId, deptId, teamId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Team deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<APIResource<List<TeamDto>>> getTeams(
            @PathVariable UUID orgId,
            @PathVariable UUID deptId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        List<TeamDto> response = teamService.getTeams(orgId, deptId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Teams retrieved successfully", response));
    }
}
