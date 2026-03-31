package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgJobTitleMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobTitleDto;
import io.github.gvn2012.org_service.entities.Department;
import io.github.gvn2012.org_service.entities.OrgJobTitle;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.repositories.DepartmentRepository;
import io.github.gvn2012.org_service.repositories.OrgJobTitleRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgJobTitleService;
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
public class OrgJobTitleServiceImpl implements IOrgJobTitleService {

    private final OrgJobTitleRepository jobTitleRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public OrgJobTitleDto createJobTitle(UUID orgId, CreateOrgJobTitleRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (jobTitleRepository.findByOrganization_IdAndCode(orgId, request.getCode()).isPresent()) {
            throw new BadRequestException("Job title code already exists in this organization");
        }

        OrgJobTitle jobTitle = OrgJobTitleMapper.toEntity(request);
        jobTitle.setOrganization(organization);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            jobTitle.setDepartment(department);
        }

        OrgJobTitle savedJobTitle = jobTitleRepository.save(jobTitle);
        return OrgJobTitleMapper.toDto(savedJobTitle);
    }

    @Override
    @Transactional
    public OrgJobTitleDto updateJobTitle(UUID orgId, UUID jobTitleId, UpdateOrgJobTitleRequest request) {
        OrgJobTitle jobTitle = jobTitleRepository.findByIdAndOrganization_Id(jobTitleId, orgId)
                .orElseThrow(() -> new NotFoundException("Job title not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            jobTitle.setName(request.getName());
        }

        if (request.getDescription() != null) {
            jobTitle.setDescription(request.getDescription());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            jobTitle.setDepartment(department);
        }

        if (request.getDisplayOrder() != null) {
            jobTitle.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getActive() != null) {
            jobTitle.setActive(request.getActive());
        }

        OrgJobTitle updatedJobTitle = jobTitleRepository.save(jobTitle);
        return OrgJobTitleMapper.toDto(updatedJobTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgJobTitleDto getJobTitleById(UUID orgId, UUID jobTitleId) {
        OrgJobTitle jobTitle = jobTitleRepository.findByIdAndOrganization_Id(jobTitleId, orgId)
                .orElseThrow(() -> new NotFoundException("Job title not found"));
        return OrgJobTitleMapper.toDto(jobTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgJobTitleDto> getJobTitlesByOrgId(UUID orgId) {
        return jobTitleRepository.findByOrganization_IdAndActiveTrue(orgId).stream()
                .map(OrgJobTitleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteJobTitle(UUID orgId, UUID jobTitleId) {
        OrgJobTitle jobTitle = jobTitleRepository.findByIdAndOrganization_Id(jobTitleId, orgId)
                .orElseThrow(() -> new NotFoundException("Job title not found"));
        jobTitle.setActive(false);
        jobTitleRepository.save(jobTitle);
    }
}
