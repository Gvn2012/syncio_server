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
@RequestMapping("/api/v1/relationship-service/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final IRelationshipService relationshipService;

    @PostMapping("/follow/{tid}")
    public ResponseEntity<APIResource<RelationshipResponse>> follow(
            @RequestHeader("X-User-Id") UUID sourceId,
            @PathVariable("tid") UUID targetId) {
        APIResource<RelationshipResponse> response = relationshipService.follow(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @PostMapping("/unfollow/{tid}")
    public ResponseEntity<APIResource<Void>> unfollow(
            @RequestHeader("X-User-Id") UUID sourceId,
            @PathVariable("tid") UUID targetId) {
        APIResource<Void> response = relationshipService.unfollow(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @GetMapping("/followers/{uid}")
    public ResponseEntity<APIResource<List<UUID>>> getFollowers(
            @PathVariable("uid") UUID userId) {
        APIResource<List<UUID>> response = relationshipService.getFollowersIds(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @GetMapping("/following/{uid}")
    public ResponseEntity<APIResource<List<UUID>>> getFollowing(
            @PathVariable("uid") UUID userId) {
        APIResource<List<UUID>> response = relationshipService.getFollowingIds(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @GetMapping("/{sid}/following/{tid}")
    public ResponseEntity<APIResource<Boolean>> isFollowing(
            @PathVariable("sid") UUID sourceId,
            @PathVariable("tid") UUID targetId) {
        APIResource<Boolean> response = relationshipService.isFollowing(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @GetMapping("/{sid}/blocked/{tid}")
    public ResponseEntity<APIResource<Boolean>> isBlocked(
            @PathVariable("sid") UUID sourceId,
            @PathVariable("tid") UUID targetId) {
        APIResource<Boolean> response = relationshipService.isBlocked(sourceId, targetId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }
}
