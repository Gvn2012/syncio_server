package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;

public interface RoleServiceInterface {
    APIResource<GetUserRoleResponse> getUserRole(String userId);
}
