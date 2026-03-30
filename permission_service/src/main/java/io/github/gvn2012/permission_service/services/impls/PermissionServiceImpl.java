package io.github.gvn2012.permission_service.services.impls;

import io.github.gvn2012.permission_service.dtos.requests.PermissionCheckRequest;
import io.github.gvn2012.permission_service.entities.Permission;
import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;
import io.github.gvn2012.permission_service.repositories.RoleRepository;
import io.github.gvn2012.permission_service.services.interfaces.PermissionServiceInterface;
import io.github.gvn2012.permission_service.services.interfaces.RoutePermissionRegistryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionServiceInterface {
    private final RoleRepository roleRepository;
    private final RoutePermissionRegistryInterface routeRegistry;

    @Transactional(readOnly = true)
    public PermissionDecision evaluateAccess(String callerId, String roleCode, String permissionCode, String targetId) {

        log.debug("Evaluating access -> callerId: {}, roleCode: {}, permissionCode: {}, targetId: {}",
                callerId, roleCode, permissionCode, targetId);

        // 1. Fetch the Role from the database
        log.debug("Step 1: Fetching role details for roleCode: {}", roleCode);
        var callerRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.error("Access evaluation failed: Role '{}' not found in database", roleCode);
                    return new RuntimeException("Role not found"); // Adjust to your specific exception
                });

        // 2. Get all permissions linked to this role
        var allUserPermissions = getAllInheritedPermissions(callerRole);
        log.debug("Step 2: Retrieved {} inherited permissions for role: {}", allUserPermissions.size(), roleCode);

        // 3. Find the requested permission in the list
        var requestedPermission = allUserPermissions.stream()
                .filter(p -> p.getCode().equals(permissionCode))
                .findFirst()
                .orElse(null);

        if (requestedPermission == null) {
            log.warn("Decision: DENY -> Permission '{}' not mapped to role '{}'", permissionCode, roleCode);
            return PermissionDecision.DENY;
        }

        log.debug("Step 3: Permission '{}' found. Requires ownership: {}",
                permissionCode, requestedPermission.getRequiresOwnership());

        // 4. Ownership Check!
        if (requestedPermission.getRequiresOwnership()) {

            // Check if higher privilege (e.g., Admin)
            if (isHigherPrivilegeRole(callerRole)) {
                log.info("Decision: PERMIT -> Role '{}' bypassed ownership check for permission '{}'",
                        roleCode, permissionCode);
                return PermissionDecision.PERMIT;
            }

            log.debug("Step 4: Evaluating ownership for resource: '{}', targetId: {}",
                    requestedPermission.getResource(), targetId);

            // Java 21 Switch Expression evaluates ownership
            var isOwner = switch (requestedPermission.getResource()) {
                case "user", "user:profile", "user:address", "user:phone", "user:email", "user:contact" ->
                    callerId.equals(targetId);
                case "post" -> verifyPostOwnership(callerId, targetId);
                default -> {
                    log.warn("Ownership evaluation bypassed: Unknown resource type '{}'",
                            requestedPermission.getResource());
                    yield false;
                }
            };

            if (isOwner) {
                log.info("Decision: PERMIT -> Caller '{}' is the verified owner of {} '{}'",
                        callerId, requestedPermission.getResource(), targetId);
                return PermissionDecision.PERMIT;
            } else {
                log.warn("Decision: DENY -> Caller '{}' attempted to access {} '{}' without ownership",
                        callerId, requestedPermission.getResource(), targetId);
                return PermissionDecision.DENY;
            }
        }

        // Default permit if no ownership is required
        log.info("Decision: PERMIT -> Permission '{}' granted to caller '{}' (No ownership required)",
                permissionCode, callerId);
        return PermissionDecision.PERMIT;
    }

    private Set<Permission> getAllInheritedPermissions(Role role) {
        var permissions = new HashSet<>(role.getPermissions());
        for (var includedRole : role.getIncludedRoles()) {
            permissions.addAll(getAllInheritedPermissions(includedRole));
        }
        return permissions;
    }

    private boolean isHigherPrivilegeRole(Role role) {
        return switch (role.getCode()) {
            case "SUPER_ADMIN", "ADMIN" -> true;
            default -> false;
        };
    }

    private boolean verifyPostOwnership(String callerId, String targetId) {
        return targetId.equals(callerId);
    }

    public PermissionDecision authorizeRequest(PermissionCheckRequest request) {

        var resolvedOpt = routeRegistry.resolve(String.valueOf(request.getHttpMethod()), request.getRequestPath());

        if (resolvedOpt.isEmpty()) {

            log.error("Unmapped route requested: {} {}", request.getHttpMethod(), request.getRequestPath());
            return PermissionDecision.DENY;
        }

        var resolved = resolvedOpt.get();
        return evaluateAccess(
                request.getUserId(),
                request.getUserRole(),
                resolved.permissionCode(),
                resolved.targetId());
    }
}
