package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgJobTitleRequest;
import io.github.gvn2012.org_service.dtos.responses.OrgJobTitleDto;
import java.util.List;
import java.util.UUID;

public interface IOrgJobTitleService {
    OrgJobTitleDto createJobTitle(UUID orgId, CreateOrgJobTitleRequest request);
    OrgJobTitleDto updateJobTitle(UUID orgId, UUID jobTitleId, UpdateOrgJobTitleRequest request);
    OrgJobTitleDto getJobTitleById(UUID orgId, UUID jobTitleId);
    List<OrgJobTitleDto> getJobTitlesByOrgId(UUID orgId);
    void deleteJobTitle(UUID orgId, UUID jobTitleId);
}
