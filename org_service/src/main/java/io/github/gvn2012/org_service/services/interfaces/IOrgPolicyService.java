package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgPolicyRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgPolicyDto;
import io.github.gvn2012.org_service.entities.enums.PolicyCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IOrgPolicyService {
    OrgPolicyDto createPolicy(UUID orgId, UUID approvedById, CreateOrgPolicyRequest request);
    OrgPolicyDto updatePolicy(UUID orgId, UUID policyId, UpdateOrgPolicyRequest request);
    OrgPolicyDto getPolicyById(UUID orgId, UUID policyId);
    Page<OrgPolicyDto> getPoliciesByOrgId(UUID orgId, Pageable pageable);
    Page<OrgPolicyDto> getPoliciesByOrgIdAndCategory(UUID orgId, PolicyCategory category, Pageable pageable);
    Page<OrgPolicyDto> getActivePoliciesByOrgId(UUID orgId, Pageable pageable);
    void deletePolicy(UUID orgId, UUID policyId);
}
