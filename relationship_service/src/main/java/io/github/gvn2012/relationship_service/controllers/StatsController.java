package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.UserRelationshipStats;
import io.github.gvn2012.relationship_service.services.interfaces.IUserRelationshipStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final IUserRelationshipStatsService statsService;

    @GetMapping("/{userId}")
    public ResponseEntity<APIResource<UserRelationshipStats>> getStats(
            @PathVariable UUID userId
    ) {
        UserRelationshipStats stats = statsService.getStatsByUserId(userId);
        return ResponseEntity.ok(APIResource.ok("Stats retrieved", stats));
    }

    @PostMapping("/{userId}/recalculate")
    public ResponseEntity<APIResource<Void>> recalculate(
            @PathVariable UUID userId
    ) {
        statsService.recalculateStats(userId);
        return ResponseEntity.ok(APIResource.message("Stats recalculation triggered", org.springframework.http.HttpStatus.OK));
    }
}
