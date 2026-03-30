package io.github.gvn2012.permission_service.dtos.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionCheckRequest {
    private String userId;
    private String userRole;
    private String requestPath;
    private String httpMethod;
}
