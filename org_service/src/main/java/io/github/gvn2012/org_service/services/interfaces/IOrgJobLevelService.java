package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobLevelRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobLevelDto;
import java.util.List;
import java.util.UUID;

public interface IOrgJobLevelService {
    OrgJobLevelDto createJobLevel(UUID orgId, CreateOrgJobLevelRequest request);
    OrgJobLevelDto updateJobLevel(UUID orgId, UUID jobLevelId, UpdateOrgJobLevelRequest request);
    OrgJobLevelDto getJobLevelById(UUID orgId, UUID jobLevelId);
    List<OrgJobLevelDto> getJobLevelsByOrgId(UUID orgId);
    void deleteJobLevel(UUID orgId, UUID jobLevelId);
}
