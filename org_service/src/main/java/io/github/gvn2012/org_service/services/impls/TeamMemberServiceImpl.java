package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.TeamMemberMapper;
import io.github.gvn2012.org_service.dtos.requests.AddTeamMemberRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamMemberRoleRequest;
import io.github.gvn2012.org_service.dtos.responses.AddTeamMemberResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamMemberDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamMemberRoleResponse;
import io.github.gvn2012.org_service.entities.Team;
import io.github.gvn2012.org_service.entities.TeamMember;
import io.github.gvn2012.org_service.entities.enums.TeamRole;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OrganizationMemberRepository;
import io.github.gvn2012.org_service.repositories.TeamMemberRepository;
import io.github.gvn2012.org_service.repositories.TeamRepository;
import io.github.gvn2012.org_service.services.interfaces.ITeamMemberService;
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
public class TeamMemberServiceImpl implements ITeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    @Transactional
    public AddTeamMemberResponse addTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId, AddTeamMemberRequest request) {
        Team team = validateTeamAccess(orgId, deptId, teamId, requestingUserId);

        // Ensure user is an active organization member
        if (!organizationMemberRepository.existsByOrganization_IdAndUserId(orgId, request.getUserId())) {
            throw new BadRequestException("User must be an organization member to be added to a team");
        }

        if (teamMemberRepository.existsByTeam_IdAndUserId(teamId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this team");
        }

        // Check team capacity
        if (team.getMaxCapacity() != null) {
            long currentCount = teamMemberRepository.countByTeam_IdAndActiveTrue(teamId);
            if (currentCount >= team.getMaxCapacity()) {
                throw new BadRequestException("Team has reached its maximum capacity");
            }
        }

        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUserId(request.getUserId());
        member.setTeamRole(request.getTeamRole() != null ? request.getTeamRole() : TeamRole.MEMBER);
        member.setJoinedAt(Instant.now());
        member.setActive(true);

        TeamMember savedMember = teamMemberRepository.save(member);
        
        return AddTeamMemberResponse.builder()
                .id(savedMember.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamMemberDto getTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId) {
        validateTeamAccess(orgId, deptId, teamId, requestingUserId);
        TeamMember member = getMemberOrThrow(memberId, teamId);
        return teamMemberMapper.toDto(member);
    }

    @Override
    @Transactional
    public UpdateTeamMemberRoleResponse updateTeamMemberRole(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId, UpdateTeamMemberRoleRequest request) {
        validateTeamAccess(orgId, deptId, teamId, requestingUserId);
        TeamMember member = getMemberOrThrow(memberId, teamId);

        if (request.getTeamRole() != null) {
            member.setTeamRole(request.getTeamRole());
        }

        TeamMember updatedMember = teamMemberRepository.save(member);
        
        return UpdateTeamMemberRoleResponse.builder()
                .id(updatedMember.getId())
                .teamRole(updatedMember.getTeamRole())
                .build();
    }

    @Override
    @Transactional
    public void removeTeamMember(UUID orgId, UUID deptId, UUID teamId, UUID memberId, UUID requestingUserId) {
        validateTeamAccess(orgId, deptId, teamId, requestingUserId);
        TeamMember member = getMemberOrThrow(memberId, teamId);
        
        member.setActive(false);
        member.setLeftAt(Instant.now());
        teamMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDto> getTeamMembers(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId) {
        validateTeamAccess(orgId, deptId, teamId, requestingUserId);
        List<TeamMember> members = teamMemberRepository.findByTeam_IdAndActiveTrue(teamId);
        return members.stream()
                .map(teamMemberMapper::toDto)
                .collect(Collectors.toList());
    }

    private Team validateTeamAccess(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId) {
        // Detailed validation hierarchy (Org -> Dept -> Team) can be performed here.
        // For now, ensuring Team exists under the specific Department is the priority.
        Team team = teamRepository.findByIdAndDepartment_Id(teamId, deptId)
                .orElseThrow(() -> new NotFoundException("Team not found"));
                
        // ensure Org access
        if (!team.getDepartment().getOrganization().getId().equals(orgId)) {
            throw new BadRequestException("Team does not belong to the specified organization");
        }
        
        return team;
    }

    private TeamMember getMemberOrThrow(UUID memberId, UUID teamId) {
        return teamMemberRepository.findByIdAndTeam_Id(memberId, teamId)
                .orElseThrow(() -> new NotFoundException("Member not found in this team"));
    }
}
