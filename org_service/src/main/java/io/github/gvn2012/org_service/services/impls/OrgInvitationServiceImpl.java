package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgInvitationMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgInvitationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgInvitationResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgInvitationDto;
import io.github.gvn2012.org_service.entities.OrgInvitation;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.OrganizationMember;
import io.github.gvn2012.org_service.entities.enums.InvitationStatus;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OrgInvitationRepository;
import io.github.gvn2012.org_service.repositories.OrganizationMemberRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgInvitationServiceImpl implements IOrgInvitationService {

    private final OrgInvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Override
    @Transactional
    public CreateOrgInvitationResponse createInvitation(UUID orgId, UUID invitedByUserId, CreateOrgInvitationRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (invitationRepository.existsByOrganization_IdAndInvitedEmailAndStatus(orgId, request.getInvitedEmail(), InvitationStatus.PENDING)) {
            throw new BadRequestException("A pending invitation already exists for this email");
        }

        String rawToken = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        String hashedToken = hashToken(rawToken);

        OrgInvitation invitation = OrgInvitationMapper.toEntity(request, invitedByUserId, hashedToken);
        invitation.setOrganization(organization);
        
        if (request.getDepartmentId() != null) {
            invitation.setDepartmentId(request.getDepartmentId());
        }
        if (request.getPositionId() != null) {
            invitation.setPositionId(request.getPositionId());
        }

        OrgInvitation savedInvitation = invitationRepository.save(invitation);

        // Normally here we would publish to Kafka for the notification service to send an email.

        return CreateOrgInvitationResponse.builder()
                .message("Invitation created successfully")
                .invitation(OrgInvitationMapper.toDto(savedInvitation))
                .inviteToken(rawToken) // Returned only once via API to the sender
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrgInvitationDto getInvitationById(UUID orgId, UUID invitationId) {
        OrgInvitation invitation = invitationRepository.findByIdAndOrganization_Id(invitationId, orgId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));
        return OrgInvitationMapper.toDto(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgInvitationDto> getInvitationsByOrgId(UUID orgId, InvitationStatus status) {
        if (status != null) {
            return invitationRepository.findByOrganization_IdAndStatus(orgId, status).stream()
                    .map(OrgInvitationMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            // Spring Data JPA repository method missing for all, fallback assuming all
            return invitationRepository.findAll().stream()
                    .filter(i -> i.getOrganization().getId().equals(orgId))
                    .map(OrgInvitationMapper::toDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void cancelInvitation(UUID orgId, UUID invitationId) {
        OrgInvitation invitation = invitationRepository.findByIdAndOrganization_Id(invitationId, orgId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Only pending invitations can be canceled");
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void acceptInvitation(UUID orgId, UUID invitationId, UUID acceptedByUserId, String token) {
        OrgInvitation invitation = invitationRepository.findByIdAndOrganization_Id(invitationId, orgId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation is no longer valid");
        }

        if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        if (!hashToken(token).equals(invitation.getTokenHash())) {
            throw new BadRequestException("Invalid invitation token");
        }

        boolean alreadyMember = organizationMemberRepository.findByOrganization_IdAndUserId(orgId, acceptedByUserId).isPresent();
        if (alreadyMember) {
            throw new BadRequestException("User is already a member of this organization");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitation.setAcceptedByUserId(acceptedByUserId);
        invitationRepository.save(invitation);

        OrganizationMember newMember = new OrganizationMember();
        newMember.setOrganization(invitation.getOrganization());
        newMember.setUserId(acceptedByUserId);
        
        organizationMemberRepository.save(newMember);
    }

    private String hashToken(String token) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
