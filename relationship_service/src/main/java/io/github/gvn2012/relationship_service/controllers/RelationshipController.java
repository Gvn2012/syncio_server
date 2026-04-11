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
@RequestMapping("/api/v1/rs/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final IRelationshipService relationshipService;
    private final io.github.gvn2012.relationship_service.services.interfaces.IUserBlockService userBlockService;

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

    @GetMapping("/status/{tid}")
    public ResponseEntity<io.github.gvn2012.relationship_service.dtos.responses.RelationshipStatusResponse> getStatus(
            @RequestHeader("X-User-Id") UUID sourceId,
            @PathVariable("tid") UUID targetId) {
        return ResponseEntity.ok(relationshipService.getRelationshipStatus(sourceId, targetId));
    }

    @GetMapping("/friends/{uid}")
    public ResponseEntity<APIResource<List<RelationshipResponse>>> getFriends(
            @PathVariable("uid") UUID userId) {
        APIResource<List<RelationshipResponse>> response = relationshipService.getFriendList(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @GetMapping("/blocks")
    public ResponseEntity<APIResource<List<UUID>>> getBlocks(
            @RequestHeader("X-User-Id") UUID userId) {
        List<UUID> blocks = userBlockService.getBlockedList(userId);
        return ResponseEntity.ok(APIResource.ok("Blocks retrieved", blocks));
    }

    @GetMapping("/blocked-by")
    public ResponseEntity<APIResource<List<UUID>>> getBlockedBy(
            @RequestHeader("X-User-Id") UUID userId) {
        List<UUID> blockedBy = userBlockService.getBlockedByList(userId);
        return ResponseEntity.ok(APIResource.ok("Blocked-by retrieved", blockedBy));
    }
}
