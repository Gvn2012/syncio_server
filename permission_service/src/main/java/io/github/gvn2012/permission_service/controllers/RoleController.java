package io.github.gvn2012.permission_service.controllers;

import io.github.gvn2012.permission_service.dtos.APIResource;
import io.github.gvn2012.permission_service.dtos.responses.GetUserRoleResponse;
import io.github.gvn2012.permission_service.services.interfaces.RoleServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class RoleController {

    private final RoleServiceInterface roleService;

    @GetMapping("/user/{uid}/role")
    public ResponseEntity<APIResource<List<GetUserRoleResponse>>> getUserRole(@PathVariable("uid") String userId) {
        log.info("Request to get roles for user: {}", userId);
        APIResource<List<GetUserRoleResponse>> response = roleService.getUserRole(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/user/{uid}/initRole")
    public ResponseEntity<APIResource<Boolean>> initUserRole(@PathVariable("uid") String userId,
            @RequestParam("registrationType") String registrationType) {
        APIResource<Boolean> response = roleService.initUserRole(userId, registrationType);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
