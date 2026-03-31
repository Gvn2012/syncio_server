package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateOrgJobLevelResponse {
    private String message;
    private OrgJobLevelDto jobLevel;
}
