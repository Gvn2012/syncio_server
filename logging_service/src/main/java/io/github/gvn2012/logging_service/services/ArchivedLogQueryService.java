package io.github.gvn2012.logging_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.logging_service.config.LogArchiveProperties;
import io.github.gvn2012.logging_service.dtos.ArchivedLogSearchResponse;
import io.github.gvn2012.logging_service.entities.CentralizedLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ArchivedLogQueryService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ObjectMapper objectMapper;
    private final LogArchiveProperties properties;

    public ArchivedLogSearchResponse search(
            String service,
            String severity,
            String traceId,
            String requestId,
            String userId,
            String action,
            String outcome,
            Instant from,
            Instant to,
            int limit) {
        Path root = Path.of(properties.getDirectory());
        if (!Files.exists(root)) {
            return ArchivedLogSearchResponse.builder()
                    .items(List.of())
                    .scannedFiles(0)
                    .matchedItems(0)
                    .build();
        }

        List<CentralizedLog> matches = new ArrayList<>();
        long scannedFiles = 0;
        try (Stream<Path> stream = Files.list(root).sorted()) {
            for (Path file : stream.toList()) {
                if (!file.getFileName().toString().endsWith(".jsonl")) {
                    continue;
                }
                if (!dateFits(file, from, to, properties.getZoneOffset())) {
                    continue;
                }
                scannedFiles++;
                try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        CentralizedLog item = objectMapper.readValue(line, CentralizedLog.class);
                        if (matches(item, service, severity, traceId, requestId, userId, action, outcome, from, to)) {
                            matches.add(item);
                            if (matches.size() >= limit) {
                                return ArchivedLogSearchResponse.builder()
                                        .items(matches)
                                        .scannedFiles(scannedFiles)
                                        .matchedItems(matches.size())
                                        .build();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to search archived log files", e);
        }

        return ArchivedLogSearchResponse.builder()
                .items(matches)
                .scannedFiles(scannedFiles)
                .matchedItems(matches.size())
                .build();
    }

    private boolean dateFits(Path file, Instant from, Instant to, ZoneOffset zoneOffset) {
        String name = file.getFileName().toString().replace(".jsonl", "");
        LocalDate fileDate = LocalDate.parse(name, DAY_FORMATTER);
        if (from != null && fileDate.isBefore(from.atOffset(zoneOffset).toLocalDate())) {
            return false;
        }
        return to == null || !fileDate.isAfter(to.atOffset(zoneOffset).toLocalDate());
    }

    private boolean matches(
            CentralizedLog item,
            String service,
            String severity,
            String traceId,
            String requestId,
            String userId,
            String action,
            String outcome,
            Instant from,
            Instant to) {
        return equalsOrBlank(service, item.getService())
                && equalsOrBlank(severity, item.getSeverity())
                && equalsOrBlank(traceId, item.getTraceId())
                && equalsOrBlank(requestId, item.getRequestId())
                && equalsOrBlank(userId, item.getUserId())
                && equalsOrBlank(action, item.getAction())
                && equalsOrBlank(outcome, item.getOutcome())
                && (from == null || !item.getTimestamp().isBefore(from))
                && (to == null || !item.getTimestamp().isAfter(to));
    }

    private boolean equalsOrBlank(String expected, String actual) {
        return expected == null || expected.isBlank() || expected.equals(actual);
    }
}
