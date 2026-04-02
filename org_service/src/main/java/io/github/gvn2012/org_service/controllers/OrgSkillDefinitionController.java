package io.github.gvn2012.org_service.controllers;

import io.github.gvn2012.org_service.dtos.requests.CreateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOrgSkillDefinitionRequest;
import io.github.gvn2012.org_service.dtos.responses.CreateOrgSkillDefinitionResponse;
import io.github.gvn2012.org_service.dtos.responses.OrgSkillDefinitionDto;
import io.github.gvn2012.org_service.dtos.responses.UpdateOrgSkillDefinitionResponse;
import io.github.gvn2012.org_service.services.interfaces.IOrgSkillDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{oid}/skill-definitions")
@RequiredArgsConstructor
public class OrgSkillDefinitionController {

    private final IOrgSkillDefinitionService skillDefinitionService;

    @PostMapping
    public ResponseEntity<CreateOrgSkillDefinitionResponse> createSkillDefinition(
            @PathVariable("oid") UUID orgId,
            @Valid @RequestBody CreateOrgSkillDefinitionRequest request) {
        OrgSkillDefinitionDto dto = skillDefinitionService.createSkillDefinition(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CreateOrgSkillDefinitionResponse.builder()
                        .message("Skill definition created successfully")
                        .skillDefinition(dto)
                        .build()
        );
    }

    @PutMapping("/{skid}")
    public ResponseEntity<UpdateOrgSkillDefinitionResponse> updateSkillDefinition(
            @PathVariable("oid") UUID orgId,
            @PathVariable("skid") UUID skillDefinitionId,
            @Valid @RequestBody UpdateOrgSkillDefinitionRequest request) {
        OrgSkillDefinitionDto dto = skillDefinitionService.updateSkillDefinition(orgId, skillDefinitionId, request);
        return ResponseEntity.ok(
                UpdateOrgSkillDefinitionResponse.builder()
                        .message("Skill definition updated successfully")
                        .skillDefinition(dto)
                        .build()
        );
    }

    @GetMapping("/{skid}")
    public ResponseEntity<OrgSkillDefinitionDto> getSkillDefinitionById(
            @PathVariable("oid") UUID orgId,
            @PathVariable("skid") UUID skillDefinitionId) {
        return ResponseEntity.ok(skillDefinitionService.getSkillDefinitionById(orgId, skillDefinitionId));
    }

    @GetMapping
    public ResponseEntity<List<OrgSkillDefinitionDto>> getSkillDefinitionsByOrgId(
            @PathVariable("oid") UUID orgId,
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(skillDefinitionService.getSkillDefinitionsByOrgIdAndCategory(orgId, category));
        }
        return ResponseEntity.ok(skillDefinitionService.getSkillDefinitionsByOrgId(orgId));
    }

    @DeleteMapping("/{skid}")
    public ResponseEntity<Void> deleteSkillDefinition(
            @PathVariable("oid") UUID orgId,
            @PathVariable("skid") UUID skillDefinitionId) {
        skillDefinitionService.deleteSkillDefinition(orgId, skillDefinitionId);
        return ResponseEntity.noContent().build();
    }
}
