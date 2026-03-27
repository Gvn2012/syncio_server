package io.github.gvn2012.permission_service.dtos.requests;

import io.github.gvn2012.permission_service.entities.enums.RoleScope;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class PermissionCheckRequest {
    private UUID userId;
    private String permissionCode;
    private String resource;
    private String action;
    private UUID resourceId;
    private UUID resourceOwnerId;
    private RoleScope scope;
    private UUID scopeId;
    private Map<String, Object> context;
}
