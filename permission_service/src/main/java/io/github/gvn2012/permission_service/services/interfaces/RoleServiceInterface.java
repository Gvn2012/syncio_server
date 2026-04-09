package io.github.gvn2012.permission_service.services.interfaces;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import java.util.List;

public interface RoleServiceInterface {
    APIResource<List<GetUserRoleResponse>> getUserRole(String userId);

    APIResource<Boolean> initUserRole(String userId, String registrationType);
}
