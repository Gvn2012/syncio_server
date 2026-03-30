package io.github.gvn2012.permission_service.seeders;

import io.github.gvn2012.permission_service.entities.Permission;
import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.repositories.PermissionRepository;
import io.github.gvn2012.permission_service.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after the main PermissionSeeder
public class UserServicePermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding specialized User Service permissions...");

        // 1. Define new permissions
        List<Permission> newPermissions = new ArrayList<>();
        
        // Address permissions
        newPermissions.add(createPermission("user:address:create", "Create User Address", "user:address", "create", "user", 2, true));
        newPermissions.add(createPermission("user:address:read", "Read User Address", "user:address", "read", "user", 1, true));
        newPermissions.add(createPermission("user:address:update", "Update User Address", "user:address", "update", "user", 2, true));
        newPermissions.add(createPermission("user:address:delete", "Delete User Address", "user:address", "delete", "user", 3, true));
        newPermissions.add(createPermission("user:address:set_primary", "Set Primary User Address", "user:address", "set_primary", "user", 2, true));

        // Phone permissions
        newPermissions.add(createPermission("user:phone:create", "Create User Phone", "user:phone", "create", "user", 2, true));
        newPermissions.add(createPermission("user:phone:read", "Read User Phone", "user:phone", "read", "user", 1, true));
        newPermissions.add(createPermission("user:phone:update", "Update User Phone", "user:phone", "update", "user", 2, true));
        newPermissions.add(createPermission("user:phone:delete", "Delete User Phone", "user:phone", "delete", "user", 3, true));
        newPermissions.add(createPermission("user:phone:set_primary", "Set Primary User Phone", "user:phone", "set_primary", "user", 2, true));

        // Email permissions
        newPermissions.add(createPermission("user:email:create", "Create User Email", "user:email", "create", "user", 2, true));
        newPermissions.add(createPermission("user:email:read", "Read User Email", "user:email", "read", "user", 1, true));
        newPermissions.add(createPermission("user:email:update", "Update User Email", "user:email", "update", "user", 2, true));
        newPermissions.add(createPermission("user:email:delete", "Delete User Email", "user:email", "delete", "user", 3, true));
        newPermissions.add(createPermission("user:email:set_primary", "Set Primary User Email", "user:email", "set_primary", "user", 2, true));

        // Emergency Contact permissions
        newPermissions.add(createPermission("user:contact:create", "Create User Emergency Contact", "user:contact", "create", "user", 2, true));
        newPermissions.add(createPermission("user:contact:read", "Read User Emergency Contact", "user:contact", "read", "user", 1, true));
        newPermissions.add(createPermission("user:contact:update", "Update User Emergency Contact", "user:contact", "update", "user", 2, true));
        newPermissions.add(createPermission("user:contact:delete", "Delete User Emergency Contact", "user:contact", "delete", "user", 3, true));
        newPermissions.add(createPermission("user:contact:set_primary", "Set Primary User Emergency Contact", "user:contact", "set_primary", "user", 2, true));

        // Profile permissions
        newPermissions.add(createPermission("user:profile:read", "Read User Profile", "user:profile", "read", "user", 1, true));
        newPermissions.add(createPermission("user:profile:update", "Update User Profile", "user:profile", "update", "user", 2, true));

        // 2. Save only permissions that don't exist yet
        List<Permission> persistedPermissions = new ArrayList<>();
        for (Permission p : newPermissions) {
            permissionRepository.findByCode(p.getCode())
                .ifPresentOrElse(
                    persistedPermissions::add,
                    () -> persistedPermissions.add(permissionRepository.save(p))
                );
        }

        // 3. Assign all to USER role
        roleRepository.findByCode("USER").ifPresent(userRole -> {
            boolean added = false;
            for (Permission p : persistedPermissions) {
                if (!userRole.getPermissions().contains(p)) {
                    userRole.getPermissions().add(p);
                    added = true;
                }
            }
            if (added) {
                roleRepository.save(userRole);
                log.info("Successfully assigned {} permissions to USER role", persistedPermissions.size());
            } else {
                log.info("USER role already has all assigned permissions");
            }
        });
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
}
