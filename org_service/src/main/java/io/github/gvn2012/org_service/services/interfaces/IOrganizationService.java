package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrganizationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrganizationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrganizationResponse;

import java.util.List;
import java.util.UUID;

public interface IOrganizationService {
    
    CreateOrganizationResponse createOrganization(UUID requestingUserId, CreateOrganizationRequest request);
    
    OrganizationDto getOrganization(UUID orgId);
    
    UpdateOrganizationResponse updateOrganization(UUID orgId, UUID requestingUserId, UpdateOrganizationRequest request);
    
    void deleteOrganization(UUID orgId, UUID requestingUserId);
    
    List<OrganizationDto> getOrgsByOwner(UUID ownerId);
    
    // OrganizationDetailResponse getOrganizationDetail(UUID orgId); // To be implemented with associations
}
