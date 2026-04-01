package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.enums.MuteScope;
import io.github.gvn2012.relationship_service.services.interfaces.IUserMuteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mutes")
@RequiredArgsConstructor
public class UserMuteController {

    private final IUserMuteService muteService;

    @PostMapping("/{mutedId}")
    public ResponseEntity<APIResource<Void>> mute(
            @RequestHeader("X-User-Id") UUID muterId,
            @PathVariable UUID mutedId,
            @RequestParam(required = false) MuteScope scope,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt
    ) {
        APIResource<Void> response = muteService.muteUser(muterId, mutedId, scope != null ? scope : MuteScope.ALL, expiresAt);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @DeleteMapping("/{mutedId}")
    public ResponseEntity<APIResource<Void>> unmute(
            @RequestHeader("X-User-Id") UUID muterId,
            @PathVariable UUID mutedId
    ) {
        APIResource<Void> response = muteService.unmuteUser(muterId, mutedId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }
}
