package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.dtos.requests.*;
import io.github.gvn2012.permission_service.dtos.responses.*;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;
import io.github.gvn2012.permission_service.entities.enums.RoleScope;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionServiceInterface {
    PermissionDecision evaluateAccess(String callerId, String roleCode, String permissionCode, String targetId);
    PermissionDecision authorizeRequest(PermissionCheckRequest request);
}
