package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.AddMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateMemberRoleResponse;

import java.util.List;
import java.util.UUID;

public interface IOrganizationMemberService {
    
    AddMemberResponse addMember(UUID orgId, UUID requestingUserId, AddMemberRequest request);
    
    OrganizationMemberDto getMember(UUID orgId, UUID memberId, UUID requestingUserId);
    
    UpdateMemberRoleResponse updateMemberRole(UUID orgId, UUID memberId, UUID requestingUserId, UpdateMemberRoleRequest request);
    
    void removeMember(UUID orgId, UUID memberId, UUID requestingUserId);
    
    List<OrganizationMemberDto> getMembers(UUID orgId, UUID requestingUserId);
}
