package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOfficeLocationResponse;
import io.github.gvn2012.org_service.dtos.responses.OfficeLocationDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOfficeLocationResponse;
import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import io.github.gvn2012.org_service.services.interfaces.IOfficeLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/office-locations")
@RequiredArgsConstructor
public class OfficeLocationController {

    private final IOfficeLocationService officeLocationService;

    @PostMapping
    public ResponseEntity<CreateOfficeLocationResponse> createOfficeLocation(
            @PathVariable("oid") UUID orgId,
            @Valid @RequestBody CreateOfficeLocationRequest request) {
        OfficeLocationDto dto = officeLocationService.createOfficeLocation(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOfficeLocationResponse.builder()
                        .message("Office location created successfully")
                        .officeLocation(dto)
                        .build()
        );
    }

    @PutMapping("/{locid}")
    public ResponseEntity<UpdateOfficeLocationResponse> updateOfficeLocation(
            @PathVariable("oid") UUID orgId,
            @PathVariable("locid") UUID officeLocationId,
            @Valid @RequestBody UpdateOfficeLocationRequest request) {
        OfficeLocationDto dto = officeLocationService.updateOfficeLocation(orgId, officeLocationId, request);
        return ResponseEntity.ok(
                UpdateOfficeLocationResponse.builder()
                        .message("Office location updated successfully")
                        .officeLocation(dto)
                        .build()
        );
    }

    @GetMapping("/{locid}")
    public ResponseEntity<OfficeLocationDto> getOfficeLocationById(
            @PathVariable("oid") UUID orgId,
            @PathVariable("locid") UUID officeLocationId) {
        return ResponseEntity.ok(officeLocationService.getOfficeLocationById(orgId, officeLocationId));
    }

    @GetMapping
    public ResponseEntity<List<OfficeLocationDto>> getOfficeLocationsByOrgId(
            @PathVariable("oid") UUID orgId,
            @RequestParam(required = false) OfficeStatus status) {
        if (status != null) {
            return ResponseEntity.ok(officeLocationService.getOfficeLocationsByOrgIdAndStatus(orgId, status));
        }
        return ResponseEntity.ok(officeLocationService.getOfficeLocationsByOrgId(orgId));
    }

    @GetMapping("/headquarters")
    public ResponseEntity<OfficeLocationDto> getHeadquartersByOrgId(
            @PathVariable("oid") UUID orgId) {
        return ResponseEntity.ok(officeLocationService.getHeadquartersByOrgId(orgId));
    }

    @DeleteMapping("/{locid}")
    public ResponseEntity<Void> deleteOfficeLocation(
            @PathVariable("oid") UUID orgId,
            @PathVariable("locid") UUID officeLocationId) {
        officeLocationService.deleteOfficeLocation(orgId, officeLocationId);
        return ResponseEntity.noContent().build();
    }
}
