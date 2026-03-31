package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateTeamRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateTeamResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamResponse;

import java.util.List;
import java.util.UUID;

public interface ITeamService {
    
    CreateTeamResponse createTeam(UUID orgId, UUID deptId, UUID requestingUserId, CreateTeamRequest request);
    
    TeamDto getTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId);
    
    UpdateTeamResponse updateTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId, UpdateTeamRequest request);
    
    void deleteTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId);
    
    List<TeamDto> getTeams(UUID orgId, UUID deptId, UUID requestingUserId);
}
