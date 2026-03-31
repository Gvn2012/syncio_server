package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.AddTeamMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddTeamMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamMemberRoleResponse;

import java.util.List;
import java.util.UUID;

public interface ITeamMemberService {
    
    AddTeamMemberResponse addTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId, AddTeamMemberRequest request);
    
    TeamMemberDto getTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId);
    
    UpdateTeamMemberRoleResponse updateTeamMemberRole(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId, UpdateTeamMemberRoleRequest request);
    
    void removeTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId);
    
    List<TeamMemberDto> getTeamMembers(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId);
}
