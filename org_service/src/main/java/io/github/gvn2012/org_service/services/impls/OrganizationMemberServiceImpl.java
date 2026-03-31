package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrganizationMemberMapper;
import io.github.gvn2012.org_service.dtos.requests.AddMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.OrganizationMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateMemberRoleResponse;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.OrganizationMember;
import io.github.gvn2012.org_service.entities.enums.MembershipStatus;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OrganizationMemberRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrganizationMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationMemberServiceImpl implements IOrganizationMemberService {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberMapper organizationMemberMapper;

    @Override
    @Transactional
    public AddMemberResponse addMember(UUID orgId, UUID requestingUserId, AddMemberRequest request) {
        Organization org = validateOrgAccess(orgId, requestingUserId);

        if (organizationMemberRepository.existsByOrganization_IdAndUserId(orgId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this organization");
        }

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(org);
        member.setUserId(request.getUserId());
        member.setStatus(MembershipStatus.ACTIVE);
        member.setOrgRole(request.getOrgRole());
        member.setJoinedAt(Instant.now());
        member.setInvitedByUserId(requestingUserId);

        OrganizationMember savedMember = organizationMemberRepository.save(member);
        
        return AddMemberResponse.builder()
                .id(savedMember.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationMemberDto getMember(UUID orgId, UUID memberId, UUID requestingUserId) {
        validateOrgAccess(orgId, requestingUserId);
        OrganizationMember member = getMemberOrThrow(memberId, orgId);
        return organizationMemberMapper.toDto(member);
    }

    @Override
    @Transactional
    public UpdateMemberRoleResponse updateMemberRole(UUID orgId, UUID memberId, UUID requestingUserId, UpdateMemberRoleRequest request) {
        validateOrgAccess(orgId, requestingUserId);
        OrganizationMember member = getMemberOrThrow(memberId, orgId);

        if (request.getOrgRole() != null) {
            member.setOrgRole(request.getOrgRole());
        }

        OrganizationMember updatedMember = organizationMemberRepository.save(member);
        
        return UpdateMemberRoleResponse.builder()
                .id(updatedMember.getId())
                .orgRole(updatedMember.getOrgRole())
                .build();
    }

    @Override
    @Transactional
    public void removeMember(UUID orgId, UUID memberId, UUID requestingUserId) {
        validateOrgAccess(orgId, requestingUserId);
        OrganizationMember member = getMemberOrThrow(memberId, orgId);
        
        member.setStatus(MembershipStatus.INACTIVE);
        member.setLeftAt(Instant.now());
        organizationMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberDto> getMembers(UUID orgId, UUID requestingUserId) {
        validateOrgAccess(orgId, requestingUserId);
        List<OrganizationMember> members = organizationMemberRepository.findByOrganization_Id(orgId);
        return members.stream()
                .map(organizationMemberMapper::toDto)
                .collect(Collectors.toList());
    }

    private Organization validateOrgAccess(UUID orgId, UUID requestingUserId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private OrganizationMember getMemberOrThrow(UUID memberId, UUID orgId) {
        return organizationMemberRepository.findByIdAndOrganization_Id(memberId, orgId)
                .orElseThrow(() -> new NotFoundException("Member not found in this organization"));
    }
}
