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
    public ResponseEntity<APIResource<List<RelationshipResponse>>> getFollowers(
            @PathVariable UUID userId
    ) {
        APIResource<List<RelationshipResponse>> response = relationshipService.getFollowers(userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value())).body(response);
    }
}
