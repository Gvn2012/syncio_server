package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreatePositionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdatePositionRequest;
import io.github.gvn2012.org_service.dtos.responses.CreatePositionResponse;
import io.github.gvn2012.org_service.dtos.responses.PositionDto;
import io.github.gvn2012.org_service.dtos.responses.UpdatePositionResponse;
import io.github.gvn2012.org_service.services.interfaces.IPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/positions")
@RequiredArgsConstructor
public class PositionController {

    private final IPositionService positionService;

    @PostMapping
    public ResponseEntity<CreatePositionResponse> createPosition(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreatePositionRequest request) {
        PositionDto dto = positionService.createPosition(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreatePositionResponse.builder()
                        .message("Position created successfully")
                        .position(dto)
                        .build()
        );
    }

    @PutMapping("/{positionId}")
    public ResponseEntity<UpdatePositionResponse> updatePosition(
            @PathVariable UUID orgId,
            @PathVariable UUID positionId,
            @Valid @RequestBody UpdatePositionRequest request) {
        PositionDto dto = positionService.updatePosition(orgId, positionId, request);
        return ResponseEntity.ok(
                UpdatePositionResponse.builder()
                        .message("Position updated successfully")
                        .position(dto)
                        .build()
        );
    }

    @GetMapping("/{positionId}")
    public ResponseEntity<PositionDto> getPositionById(
            @PathVariable UUID orgId,
            @PathVariable UUID positionId) {
        return ResponseEntity.ok(positionService.getPositionById(orgId, positionId));
    }

    @GetMapping
    public ResponseEntity<List<PositionDto>> getPositionsByOrgId(
            @PathVariable UUID orgId,
            @RequestParam(required = false) UUID departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(positionService.getPositionsByOrgIdAndDepartmentId(orgId, departmentId));
        }
        return ResponseEntity.ok(positionService.getPositionsByOrgId(orgId));
    }

    @DeleteMapping("/{positionId}")
    public ResponseEntity<Void> deletePosition(
            @PathVariable UUID orgId,
            @PathVariable UUID positionId) {
        positionService.deletePosition(orgId, positionId);
        return ResponseEntity.noContent().build();
    }
}
