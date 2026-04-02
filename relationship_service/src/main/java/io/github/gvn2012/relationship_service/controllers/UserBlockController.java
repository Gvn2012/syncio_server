package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.enums.BlockReason;
import io.github.gvn2012.relationship_service.services.interfaces.IUserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationship-service/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final IUserBlockService blockService;

    @PostMapping("/{bid}")
    public ResponseEntity<APIResource<Void>> block(
            @RequestHeader("X-User-Id") UUID blockerId,
            @PathVariable("bid") UUID blockedId,
            @RequestParam(required = false) BlockReason reason,
            @RequestParam(required = false) String notes) {
        APIResource<Void> response = blockService.blockUser(blockerId, blockedId,
                reason != null ? reason : BlockReason.OTHER, notes);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @DeleteMapping("/{bid}")
    public ResponseEntity<APIResource<Void>> unblock(
            @RequestHeader("X-User-Id") UUID blockerId,
            @PathVariable("bid") UUID blockedId) {
        APIResource<Void> response = blockService.unblockUser(blockerId, blockedId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }
}
