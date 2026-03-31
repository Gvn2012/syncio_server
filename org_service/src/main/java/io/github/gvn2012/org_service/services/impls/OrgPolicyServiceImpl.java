package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OrgPolicyMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgPolicyDto;
import io.github.gvn2012.org_service.entities.OrgPolicy;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OrgPolicyRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOrgPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrgPolicyServiceImpl implements IOrgPolicyService {

    private final OrgPolicyRepository policyRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public OrgPolicyDto createPolicy(UUID orgId, UUID approvedById, CreateOrgPolicyRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        OrgPolicy policy = OrgPolicyMapper.toEntity(request, approvedById);
        policy.setOrganization(organization);

        OrgPolicy savedPolicy = policyRepository.save(policy);
        return OrgPolicyMapper.toDto(savedPolicy);
    }

    @Override
    @Transactional
    public OrgPolicyDto updatePolicy(UUID orgId, UUID policyId, UpdateOrgPolicyRequest request) {
        OrgPolicy policy = policyRepository.findByIdAndOrganization_Id(policyId, orgId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            policy.setTitle(request.getTitle());
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            policy.setContent(request.getContent());
        }

        if (request.getVersion() != null && !request.getVersion().isBlank()) {
            policy.setVersion(request.getVersion());
        }

        if (request.getCategory() != null) {
            policy.setCategory(request.getCategory());
        }

        if (request.getEffectiveDate() != null) {
            policy.setEffectiveDate(request.getEffectiveDate());
        }

        if (request.getActive() != null) {
            policy.setActive(request.getActive());
        }

        OrgPolicy updatedPolicy = policyRepository.save(policy);
        return OrgPolicyMapper.toDto(updatedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public OrgPolicyDto getPolicyById(UUID orgId, UUID policyId) {
        OrgPolicy policy = policyRepository.findByIdAndOrganization_Id(policyId, orgId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        return OrgPolicyMapper.toDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgPolicyDto> getPoliciesByOrgId(UUID orgId, Pageable pageable) {
        return policyRepository.findByOrganization_Id(orgId, pageable)
                .map(OrgPolicyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgPolicyDto> getPoliciesByOrgIdAndCategory(UUID orgId, PolicyCategory category, Pageable pageable) {
        return policyRepository.findByOrganization_IdAndCategory(orgId, category, pageable)
                .map(OrgPolicyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrgPolicyDto> getActivePoliciesByOrgId(UUID orgId, Pageable pageable) {
        return policyRepository.findByOrganization_IdAndActiveTrue(orgId, pageable)
                .map(OrgPolicyMapper::toDto);
    }

    @Override
    @Transactional
    public void deletePolicy(UUID orgId, UUID policyId) {
        OrgPolicy policy = policyRepository.findByIdAndOrganization_Id(policyId, orgId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        policyRepository.delete(policy);
    }
}
