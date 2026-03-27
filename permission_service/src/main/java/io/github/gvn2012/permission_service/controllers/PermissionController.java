package io.github.gvn2012.permission_service.controllers;

import io.github.gvn2012.permission_service.services.impls.PermissionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/permissions/")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PermissionController {

    private final PermissionServiceImpl permissionServiceImpl;

    @GetMapping
    public ResponseEntity<String> checkPermission() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}
