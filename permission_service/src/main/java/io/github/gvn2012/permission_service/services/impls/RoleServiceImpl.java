package io.github.gvn2012.permission_service.services.impls;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.entities.UserRole;
import io.github.gvn2012.permission_service.exceptions.NotFoundException;
import io.github.gvn2012.permission_service.repositories.RoleRepository;
import io.github.gvn2012.permission_service.repositories.UserRoleRepository;
import io.github.gvn2012.permission_service.services.interfaces.RoleServiceInterface;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleServiceInterface {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public APIResource<GetUserRoleResponse> getUserRole(String userId) {
        UserRole userRole = userRoleRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() ->
                        new NotFoundException("Role not found with id: " + userId)
                );

        GetUserRoleResponse getUserRoleResponse = new GetUserRoleResponse(
                userRole.getRole().getId().toString(),
                userRole.getRole().getName(),
                userRole.getRole().getColor(),
                userRole.getRole().getIcon(),
                userRole.getRole().getDisplayOrder()
        );
        return APIResource.ok("Get user role successfully", getUserRoleResponse);
    }

}
