package io.github.gvn2012.org_service.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrgAnnouncementResponse {
    private String message;
    private OrgAnnouncementDto announcement;
}
