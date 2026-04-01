package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipResponse;
import io.github.gvn2012.relationship_service.services.interfaces.IRelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final IRelationshipService relationshipService;

    @PostMapping("/follow/{targetId}")
    public ResponseEntity<APIResource<RelationshipResponse>> follow(
            @RequestHeader("X-User-Id") UUID sourceId,
            @PathVariable UUID targetId
    ) {
        APIResource<RelationshipResponse> response = relationshipService.follow(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @PostMapping("/unfollow/{targetId}")
    public ResponseEntity<APIResource<Void>> unfollow(
            @RequestHeader("X-User-Id") UUID sourceId,
            @PathVariable UUID targetId
    ) {
        APIResource<Void> response = relationshipService.unfollow(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<APIResource<List<UUID>>> getFollowers(
            @PathVariable UUID userId
    ) {
        APIResource<List<UUID>> response = relationshipService.getFollowersIds(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<APIResource<List<UUID>>> getFollowing(
            @PathVariable UUID userId
    ) {
        APIResource<List<UUID>> response = relationshipService.getFollowingIds(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @GetMapping("/{sourceId}/following/{targetId}")
    public ResponseEntity<APIResource<Boolean>> isFollowing(
            @PathVariable UUID sourceId,
            @PathVariable UUID targetId
    ) {
        APIResource<Boolean> response = relationshipService.isFollowing(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }

    @GetMapping("/{sourceId}/blocked/{targetId}")
    public ResponseEntity<APIResource<Boolean>> isBlocked(
            @PathVariable UUID sourceId,
            @PathVariable UUID targetId
    ) {
        APIResource<Boolean> response = relationshipService.isBlocked(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }
}
