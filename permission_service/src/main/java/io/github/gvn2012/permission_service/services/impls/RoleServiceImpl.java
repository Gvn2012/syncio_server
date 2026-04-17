package io.github.gvn2012.permission_service.services.impls;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.permission_service.entities.Role;
import io.github.gvn2012.permission_service.entities.UserRole;
import io.github.gvn2012.permission_service.entities.enums.RoleScope;
import io.github.gvn2012.permission_service.exceptions.BadRequestException;
import io.github.gvn2012.permission_service.exceptions.NotFoundException;
import io.github.gvn2012.permission_service.repositories.RoleRepository;
import io.github.gvn2012.permission_service.repositories.UserRoleRepository;
import io.github.gvn2012.permission_service.services.interfaces.RoleServiceInterface;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleServiceInterface {

        private final UserRoleRepository userRoleRepository;
        private final RoleRepository roleRepository;

        @Override
        public APIResource<List<GetUserRoleResponse>> getUserRole(String userId) {
                List<UserRole> userRoles = userRoleRepository.findAllByUserId(UUID.fromString(userId));

                List<GetUserRoleResponse> responses = userRoles.stream()
                                .map(userRole -> new GetUserRoleResponse(
                                                userRole.getRole().getId().toString(),
                                                userRole.getRole().getCode(),
                                                userRole.getRole().getColor(),
                                                userRole.getRole().getIcon(),
                                                userRole.getRole().getDisplayOrder()))
                                .collect(Collectors.toList());

                return APIResource.ok("Get user roles successfully", responses);
        }

        @Override
        @Transactional
        public APIResource<Boolean> initUserRole(String userId, String registrationType) {
                if (!"standalone".equals(registrationType) && !"admin".equals(registrationType)) {
                        throw new BadRequestException("Invalid registration type: " + registrationType);
                }

                List<String> roleCodes = new ArrayList<>();
                roleCodes.add("USER");

                if ("admin".equals(registrationType)) {
                        roleCodes.add("ORG_ADMIN");
                }

                UUID userUuid = UUID.fromString(userId);

                for (String code : roleCodes) {
                        Role role = roleRepository.findByCode(code)
                                        .orElseThrow(() -> new NotFoundException("Role not found with code: " + code));

                        UserRole userRole = UserRole.builder()
                                        .userId(userUuid)
                                        .role(role)
                                        .scope(RoleScope.GLOBAL)
                                        .isPrimary("USER".equals(code))
                                        .build();

                        userRoleRepository.save(userRole);
                }

                return APIResource.ok("User roles initialized successfully", true);
        }

}
