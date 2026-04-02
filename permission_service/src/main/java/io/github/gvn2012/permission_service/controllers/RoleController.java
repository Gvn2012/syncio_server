package io.github.gvn2012.permission_service.controllers;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.permission_service.services.interfaces.RoleServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class RoleController {

    private final RoleServiceInterface roleService;

    @GetMapping("/user/{uid}/role")
    public ResponseEntity<APIResource<GetUserRoleResponse>> getUserRole(@PathVariable("uid") String userId) {
        APIResource<GetUserRoleResponse> response = roleService.getUserRole(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
