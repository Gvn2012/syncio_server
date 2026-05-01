package io.github.gvn2012.logging_service.controllers;

import io.github.gvn2012.logging_service.dtos.CentralizedLogSearchResponse;
import io.github.gvn2012.logging_service.dtos.ArchiveRunResponse;
import io.github.gvn2012.logging_service.dtos.ArchivedLogSearchResponse;
import io.github.gvn2012.logging_service.services.ArchivedLogQueryService;
import io.github.gvn2012.logging_service.services.CentralizedLogArchiveService;
import io.github.gvn2012.logging_service.services.CentralizedLogQueryService;
import io.github.gvn2012.shared.dtos.APIResource;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class CentralizedLogController {

    private final CentralizedLogQueryService queryService;
    private final CentralizedLogArchiveService archiveService;
    private final ArchivedLogQueryService archivedLogQueryService;

    @GetMapping
    public ResponseEntity<APIResource<CentralizedLogSearchResponse>> search(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 200);

        CentralizedLogSearchResponse response = queryService.search(
                service, severity, traceId, requestId, userId, action, outcome, from, to, boundedPage, boundedSize);
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResource.ok("Centralized logs fetched successfully", response));
    }

    @GetMapping("/archives")
    public ResponseEntity<APIResource<ArchivedLogSearchResponse>> searchArchived(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "200") int limit) {
        int boundedLimit = Math.min(Math.max(limit, 1), 1000);
        ArchivedLogSearchResponse response = archivedLogQueryService.search(
                service, severity, traceId, requestId, userId, action, outcome, from, to, boundedLimit);
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResource.ok("Archived centralized logs fetched successfully", response));
    }

    @PostMapping("/archives/run")
    public ResponseEntity<APIResource<ArchiveRunResponse>> runArchive() {
        ArchiveRunResponse response = archiveService.archiveExpiredLogs();
        return ResponseEntity.status(HttpStatus.OK)
                .body(APIResource.ok("Centralized log archive completed", response));
    }
}
