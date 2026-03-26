package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.dtos.requests.*;
import io.github.gvn2012.permission_service.dtos.responses.*;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;
import io.github.gvn2012.permission_service.entities.enums.RoleScope;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionServiceInterface {

    // ================= ACCESS CHECKS =================

    PermissionDecision checkPermission(PermissionCheckRequest request);

    PermissionDecision checkPermission(UUID userId, String permissionCode);

    PermissionDecision checkPermission(UUID userId, String resource, String action);

    PermissionDecision checkPermission(UUID userId, String resource, String action, UUID resourceId);

    boolean hasPermission(UUID userId, String permissionCode);

    boolean hasAnyPermission(UUID userId, Set<String> permissionCodes);

    boolean hasAllPermissions(UUID userId, Set<String> permissionCodes);

    // ================= ROLE MANAGEMENT =================

    void assignRole(UUID userId, String roleCode, RoleScope scope, UUID scopeId, UUID assignedBy);

    void revokeRole(UUID userId, String roleCode, RoleScope scope, UUID scopeId, UUID revokedBy, String reason);

    Set<String> getUserRoles(UUID userId);

    Set<String> getUserRoles(UUID userId, RoleScope scope, UUID scopeId);

    boolean hasRole(UUID userId, String roleCode);

    boolean hasAnyRole(UUID userId, Set<String> roleCodes);

    // ================= PERMISSION QUERIES =================

    Set<String> getEffectivePermissions(UUID userId);

    Set<String> getEffectivePermissions(UUID userId, RoleScope scope, UUID scopeId);

    Set<String> getPermissionsForResource(UUID userId, String resourceType, UUID resourceId);

    // ================= RESOURCE PERMISSIONS =================

    void grantResourcePermission(GrantResourcePermissionRequest request);

    void revokeResourcePermission(UUID resourcePermissionId, UUID revokedBy);

//    List<ResourcePermissionDto> getResourcePermissions(String resourceType, UUID resourceId);

    // ================= PERMISSION OVERRIDES =================

//    void addPermissionOverride(AddPermissionOverrideRequest request);

    void removePermissionOverride(UUID overrideId);


    // ================= BULK OPERATIONS =================

    List<PermissionCheckResult> checkPermissionsBatch(List<PermissionCheckRequest> requests);

    List<UUID> filterAuthorizedUsers(Set<UUID> userIds, String permissionCode);

    List<UUID> filterAccessibleResources(UUID userId, String resourceType, Set<UUID> resourceIds, String action);
}
