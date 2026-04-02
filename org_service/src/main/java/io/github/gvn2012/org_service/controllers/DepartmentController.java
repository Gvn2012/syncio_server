package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.APIResource;
import io.github.gvn2012.org_service.dtos.requests.CreateDepartmentRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateDepartmentRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateDepartmentResponse;
import io.github.gvn2012.org_service.dtos.responses.DepartmentDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateDepartmentResponse;
import io.github.gvn2012.org_service.services.interfaces.IDepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService departmentService;

    @PostMapping
    public ResponseEntity<APIResource<CreateDepartmentResponse>> createDepartment(
            @PathVariable("oid") UUID orgId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody CreateDepartmentRequest request) {

        CreateDepartmentResponse response = departmentService.createDepartment(orgId, requestingUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResource.ok("Department created successfully", response));
    }

    @GetMapping("/{did}")
    public ResponseEntity<APIResource<DepartmentDto>> getDepartment(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        DepartmentDto response = departmentService.getDepartment(orgId, deptId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Department retrieved successfully", response));
    }

    @PutMapping("/{did}")
    public ResponseEntity<APIResource<UpdateDepartmentResponse>> updateDepartment(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @RequestHeader("X-User-Id") UUID requestingUserId,
            @Valid @RequestBody UpdateDepartmentRequest request) {

        UpdateDepartmentResponse response = departmentService.updateDepartment(orgId, deptId, requestingUserId, request);
        return ResponseEntity.ok(APIResource.ok("Department updated successfully", response));
    }

    @DeleteMapping("/{did}")
    public ResponseEntity<APIResource<Void>> deleteDepartment(
            @PathVariable("oid") UUID orgId,
            @PathVariable("did") UUID deptId,
            @RequestHeader("X-User-Id") UUID requestingUserId) {

        departmentService.deleteDepartment(orgId, deptId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Department deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<APIResource<List<DepartmentDto>>> getDepartments(
            @PathVariable("oid") UUID orgId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestingUserId) {

        List<DepartmentDto> response = departmentService.getDepartments(orgId, requestingUserId);
        return ResponseEntity.ok(APIResource.ok("Departments retrieved successfully", response));
    }
}
