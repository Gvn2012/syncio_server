package io.github.gvn2012.search_service.controllers;

import io.github.gvn2012.search_service.services.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search/internal")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/reindex")
    public ResponseEntity<String> triggerReindex() {
        syncService.triggerFullReindex();
        return ResponseEntity.ok("Re-indexing process triggered across services.");
    }
}
