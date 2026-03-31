package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgJobLevelMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobLevelDto;
import io.github.gvn2012.org_service.entities.OrgJobLevel;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.repositories.OrgJobLevelRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgJobLevelService;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgJobLevelServiceImpl implements IOrgJobLevelService {

    private final OrgJobLevelRepository jobLevelRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public OrgJobLevelDto createJobLevel(UUID orgId, CreateOrgJobLevelRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (jobLevelRepository.findByOrganization_IdAndCode(orgId, request.getCode()).isPresent()) {
            throw new BadRequestException("Job level code already exists in this organization");
        }

        OrgJobLevel jobLevel = OrgJobLevelMapper.toEntity(request);
        jobLevel.setOrganization(organization);

        OrgJobLevel savedJobLevel = jobLevelRepository.save(jobLevel);
        return OrgJobLevelMapper.toDto(savedJobLevel);
    }

    @Override
    @Transactional
    public OrgJobLevelDto updateJobLevel(UUID orgId, UUID jobLevelId, UpdateOrgJobLevelRequest request) {
        OrgJobLevel jobLevel = jobLevelRepository.findByIdAndOrganization_Id(jobLevelId, orgId)
                .orElseThrow(() -> new NotFoundException("Job level not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            jobLevel.setName(request.getName());
        }

        if (request.getDescription() != null) {
            jobLevel.setDescription(request.getDescription());
        }

        if (request.getRankOrder() != null) {
            jobLevel.setRankOrder(request.getRankOrder());
        }

        if (request.getActive() != null) {
            jobLevel.setActive(request.getActive());
        }

        OrgJobLevel updatedJobLevel = jobLevelRepository.save(jobLevel);
        return OrgJobLevelMapper.toDto(updatedJobLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgJobLevelDto getJobLevelById(UUID orgId, UUID jobLevelId) {
        OrgJobLevel jobLevel = jobLevelRepository.findByIdAndOrganization_Id(jobLevelId, orgId)
                .orElseThrow(() -> new NotFoundException("Job level not found"));
        return OrgJobLevelMapper.toDto(jobLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgJobLevelDto> getJobLevelsByOrgId(UUID orgId) {
        return jobLevelRepository.findByOrganization_IdAndActiveTrueOrderByRankOrderAsc(orgId).stream()
                .map(OrgJobLevelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteJobLevel(UUID orgId, UUID jobLevelId) {
        OrgJobLevel jobLevel = jobLevelRepository.findByIdAndOrganization_Id(jobLevelId, orgId)
                .orElseThrow(() -> new NotFoundException("Job level not found"));
        jobLevel.setActive(false);
        jobLevelRepository.save(jobLevel);
    }
}
