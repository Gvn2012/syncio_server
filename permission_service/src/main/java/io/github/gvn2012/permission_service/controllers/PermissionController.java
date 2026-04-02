package io.github.gvn2012.permission_service.controllers;

import io.github.gvn2012.permission_service.dtos.requests.PermissionCheckRequest;
import io.github.gvn2012.permission_service.services.impls.PermissionServiceImpl;
import io.github.gvn2012.permission_service.services.interfaces.RoutePermissionRegistryInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j // Added for logging warnings on unmapped routes
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PermissionController {

    private final PermissionServiceImpl permissionServiceImpl;

    private final RoutePermissionRegistryInterface routeRegistry;

    @PostMapping("/authorize")
    public ResponseEntity<Void> authorize(@RequestBody PermissionCheckRequest request) {

        log.info("Request{} {} {} {}", request.getHttpMethod().toString(), request.getRequestPath(),
                request.getUserId(), request.getUserRole());

        var resolvedOpt = routeRegistry.resolve(
                request.getHttpMethod().toString(),
                request.getRequestPath());

        log.info("ResolvedOpt Count {}", resolvedOpt.stream().count());

        if (resolvedOpt.isEmpty()) {
            log.warn("Access denied. Unmapped route requested: {} {}",
                    request.getHttpMethod(), request.getRequestPath());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var resolved = resolvedOpt.get();

        log.info("Resolved {} {}", resolved.permissionCode(), resolved.targetId());

        var decision = permissionServiceImpl.evaluateAccess(
                request.getUserId(),
                request.getUserRole(),
                resolved.permissionCode(),
                resolved.targetId());

        return switch (decision) {
            case PERMIT ->
                ResponseEntity.ok().build();

            case DENY, NOT_APPLICABLE, INDETERMINATE ->
                ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }
}