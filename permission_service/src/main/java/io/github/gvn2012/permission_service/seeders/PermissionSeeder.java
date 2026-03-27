package io.github.gvn2012.permission_service.seeders;

import io.github.gvn2012.permission_service.entities.Permission;
import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.entities.enums.RoleType;
import io.github.gvn2012.permission_service.repositories.PermissionRepository;
import io.github.gvn2012.permission_service.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (permissionRepository.count() > 0) {
            return; // already seeded
        }

        // Create permissions
        List<Permission> permissions = createPermissions();
        permissionRepository.saveAll(permissions);

        // Create roles
        createRoles(permissions);
    }

    private List<Permission> createPermissions() {
        return List.of(
                // Post permissions
                createPermission("post:create", "Create Post", "post", "create", "content", 1),
                createPermission("post:read", "Read Post", "post", "read", "content", 1),
                createPermission("post:update", "Update Post", "post", "update", "content", 2, true),
                createPermission("post:delete", "Delete Post", "post", "delete", "content", 3, true),
                createPermission("post:moderate", "Moderate Post", "post", "moderate", "moderation", 4),

                // Comment permissions
                createPermission("comment:create", "Create Comment", "comment", "create", "content", 1),
                createPermission("comment:read", "Read Comment", "comment", "read", "content", 1),
                createPermission("comment:update", "Update Comment", "comment", "update", "content", 2, true),
                createPermission("comment:delete", "Delete Comment", "comment", "delete", "content", 3, true),
                createPermission("comment:moderate", "Moderate Comment", "comment", "moderate", "moderation", 4),

                // User permissions
                createPermission("user:read", "View User Profile", "user", "read", "user", 1),
                createPermission("user:update", "Update User", "user", "update", "user", 2, true),
                createPermission("user:delete", "Delete User", "user", "delete", "admin", 5),
                createPermission("user:ban", "Ban User", "user", "ban", "moderation", 5),
                createPermission("user:suspend", "Suspend User", "user", "suspend", "moderation", 4),

                // Group permissions
                createPermission("group:create", "Create Group", "group", "create", "content", 2),
                createPermission("group:read", "View Group", "group", "read", "content", 1),
                createPermission("group:update", "Update Group", "group", "update", "content", 2, true),
                createPermission("group:delete", "Delete Group", "group", "delete", "content", 3, true),
                createPermission("group:manage_members", "Manage Group Members", "group", "manage_members", "content", 3),

                // Admin permissions
                createPermission("admin:access", "Access Admin Panel", "admin", "access", "admin", 4),
                createPermission("admin:manage_roles", "Manage Roles", "admin", "manage_roles", "admin", 5),
                createPermission("admin:manage_permissions", "Manage Permissions", "admin", "manage_permissions", "admin", 5),
                createPermission("admin:view_audit_log", "View Audit Log", "admin", "view_audit_log", "admin", 3),
                createPermission("admin:manage_settings", "Manage Settings", "admin", "manage_settings", "admin", 5)
        );
    }

    private Permission createPermission(String code, String name, String resource, String action,
                                        String category, int riskLevel) {
        return createPermission(code, name, resource, action, category, riskLevel, false);
    }

    private Permission createPermission(String code, String name, String resource, String action,
                                        String category, int riskLevel,
                                        boolean requiresOwnership) {
        Permission p = new Permission();
        p.setCode(code);
        p.setName(name);
        p.setResource(resource);
        p.setAction(action);
        p.setCategory(category);
        p.setRiskLevel(riskLevel);
        p.setRequiresOwnership(requiresOwnership);
        p.setRequiresRelationship(false);
        p.setIsSystem(true);
        p.setIsActive(true);
        return p;
    }

    private void createRoles(List<Permission> allPermissions) {
        // Guest role
        Role guest = createRole("GUEST", "Guest", RoleType.SYSTEM, 0, false);
        guest.setPermissions(filterPermissions(allPermissions, Set.of("post:read", "comment:read", "user:read", "group:read")));
        roleRepository.save(guest);

        // User role
        Role user = createRole("USER", "User", RoleType.SYSTEM, 10, true);
        user.setPermissions(filterPermissions(allPermissions, Set.of(
                "post:create", "post:read", "post:update", "post:delete",
                "comment:create", "comment:read", "comment:update", "comment:delete",
                "user:read", "user:update",
                "group:create", "group:read", "group:update", "group:delete", "group:manage_members"
        )));
        roleRepository.save(user);

        // Moderator role
        Role moderator = createRole("MODERATOR", "Moderator", RoleType.PLATFORM, 50, false);
        moderator.getIncludedRoles().add(user);
        moderator.setPermissions(filterPermissions(allPermissions, Set.of(
                "post:moderate", "comment:moderate", "user:suspend", "admin:access", "admin:view_audit_log"
        )));
        roleRepository.save(moderator);

        // Admin role
        Role admin = createRole("ADMIN", "Administrator", RoleType.PLATFORM, 90, false);
        admin.getIncludedRoles().add(moderator);
        admin.setPermissions(filterPermissions(allPermissions, Set.of(
                "user:ban", "user:delete", "admin:manage_roles", "admin:manage_settings"
        )));
        roleRepository.save(admin);

        // Super Admin role
        Role superAdmin = createRole("SUPER_ADMIN", "Super Administrator", RoleType.SYSTEM, 100, false);
        superAdmin.getIncludedRoles().add(admin);
        superAdmin.setPermissions(filterPermissions(allPermissions, Set.of("admin:manage_permissions")));
        superAdmin.setMaxUsers(5);
        roleRepository.save(superAdmin);
    }

    private Role createRole(String code, String name, RoleType type, int hierarchyLevel,
                            boolean isDefault) {
        Role r = new Role();
        r.setCode(code);
        r.setName(name);
        r.setRoleType(type);
        r.setHierarchyLevel(hierarchyLevel);
        r.setIsSystem(true);
        r.setIsDefault(isDefault);
        r.setIsActive(true);
        r.setIsAssignable(!isDefault);
        return r;
    }

    private Set<Permission> filterPermissions(List<Permission> all, Set<String> codes) {
        return new java.util.HashSet<>(
                all.stream()
                        .filter(p -> codes.contains(p.getCode()))
                        .toList()
        );
    }
}
