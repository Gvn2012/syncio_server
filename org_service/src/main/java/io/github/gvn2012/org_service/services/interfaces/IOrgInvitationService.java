package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgInvitationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgInvitationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgInvitationDto;
import io.github.gvn2012.org_service.entities.enums.InvitationStatus;

import java.util.List;
import java.util.UUID;

public interface IOrgInvitationService {
    CreateOrgInvitationResponse createInvitation(UUID orgId, UUID invitedByUserId, CreateOrgInvitationRequest request);
    OrgInvitationDto getInvitationById(UUID orgId, UUID invitationId);
    List<OrgInvitationDto> getInvitationsByOrgId(UUID orgId, InvitationStatus status);
    void cancelInvitation(UUID orgId, UUID invitationId);
    void acceptInvitation(UUID orgId, UUID invitationId, UUID acceptedByUserId, String token);
}
