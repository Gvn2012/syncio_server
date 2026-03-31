package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.TeamMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateTeamRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateTeamRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateTeamResponse;
import io.github.gvn2012.org_service.dtos.responses.TeamDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateTeamResponse;
import io.github.gvn2012.org_service.entities.Department;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.Team;
import io.github.gvn2012.org_service.entities.enums.TeamStatus;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.DepartmentRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.repositories.TeamRepository;
import io.github.gvn2012.org_service.services.interfaces.ITeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements ITeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamMapper teamMapper;

    @Override
    @Transactional
    public CreateTeamResponse createTeam(UUID orgId, UUID deptId, UUID requestingUserId, CreateTeamRequest request) {
        Department dept = validateOrgAndDept(orgId, deptId, requestingUserId);

        Team team = new Team();
        team.setDepartment(dept);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setTeamLeadId(request.getTeamLeadId());
        team.setMaxCapacity(request.getMaxCapacity());
        team.setStatus(TeamStatus.ACTIVE);

        Team savedTeam = teamRepository.save(team);
        
        return CreateTeamResponse.builder()
                .id(savedTeam.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDto getTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId) {
        validateOrgAndDept(orgId, deptId, requestingUserId);
        Team team = getTeamOrThrow(teamId, deptId);
        return teamMapper.toDto(team);
    }

    @Override
    @Transactional
    public UpdateTeamResponse updateTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId, UpdateTeamRequest request) {
        validateOrgAndDept(orgId, deptId, requestingUserId);
        Team team = getTeamOrThrow(teamId, deptId);

        if (request.getName() != null) team.setName(request.getName());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        if (request.getTeamLeadId() != null) team.setTeamLeadId(request.getTeamLeadId());
        if (request.getMaxCapacity() != null) team.setMaxCapacity(request.getMaxCapacity());

        Team updatedTeam = teamRepository.save(team);
        
        return UpdateTeamResponse.builder()
                .id(updatedTeam.getId())
                .build();
    }

    @Override
    @Transactional
    public void deleteTeam(UUID orgId, UUID deptId, UUID teamId, UUID requestingUserId) {
        validateOrgAndDept(orgId, deptId, requestingUserId);
        Team team = getTeamOrThrow(teamId, deptId);
        
        team.setStatus(TeamStatus.INACTIVE);
        teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamDto> getTeams(UUID orgId, UUID deptId, UUID requestingUserId) {
        validateOrgAndDept(orgId, deptId, requestingUserId);
        List<Team> teams = teamRepository.findByDepartment_Id(deptId);
        return teams.stream()
                .map(teamMapper::toDto)
                .collect(Collectors.toList());
    }

    private Department validateOrgAndDept(UUID orgId, UUID deptId, UUID requestingUserId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
                
        // Org owner check or member check logic can be added here
        
        return departmentRepository.findByIdAndOrganization_Id(deptId, orgId)
                .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
    }

    private Team getTeamOrThrow(UUID teamId, UUID deptId) {
        return teamRepository.findByIdAndDepartment_Id(teamId, deptId)
                .orElseThrow(() -> new NotFoundException("Team not found in this department"));
    }
}
