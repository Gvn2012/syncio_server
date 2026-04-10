package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.dtos.requests.*;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;

public interface PermissionServiceInterface {
    PermissionDecision evaluateAccess(String callerId, String roleCode, String permissionCode, String targetId);

    PermissionDecision authorizeRequest(PermissionCheckRequest request);
}
