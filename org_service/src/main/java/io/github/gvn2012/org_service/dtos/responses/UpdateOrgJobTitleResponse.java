package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateOrgJobTitleResponse {
    private String message;
    private OrgJobTitleDto jobTitle;
}
