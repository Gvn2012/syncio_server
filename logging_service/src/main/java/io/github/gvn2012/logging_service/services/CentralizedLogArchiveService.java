package io.github.gvn2012.logging_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.logging_service.config.LogArchiveProperties;
import io.github.gvn2012.logging_service.dtos.ArchiveRunResponse;
import io.github.gvn2012.logging_service.entities.CentralizedLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CentralizedLogArchiveService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    private final LogArchiveProperties properties;

    @Scheduled(cron = "${syncio.logging.archive.cron:0 15 2 * * *}")
    public void archiveScheduled() {
        if (!properties.isEnabled()) {
            return;
        }
        ArchiveRunResponse response = archiveExpiredLogs();
        if (response.getArchivedCount() > 0) {
            log.info("Archived {} centralized logs into {} file(s), deleted {} records older than {}",
                    response.getArchivedCount(),
                    response.getArchiveFileCount(),
                    response.getDeletedCount(),
                    response.getCutoffTimestamp());
        }
    }

    public ArchiveRunResponse archiveExpiredLogs() {
        Instant cutoff = Instant.now().minusSeconds(properties.getRetentionDays() * 24L * 60L * 60L);
        int totalArchived = 0;
        int totalDeleted = 0;
        int filesTouched = 0;

        try {
            Files.createDirectories(Path.of(properties.getDirectory()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create archive directory " + properties.getDirectory(), e);
        }

        while (true) {
            Query query = new Query(Criteria.where("timestamp").lt(cutoff));
            query.with(Sort.by(Sort.Direction.ASC, "timestamp"));
            query.limit(properties.getBatchSize());
            List<CentralizedLog> batch = mongoTemplate.find(query, CentralizedLog.class);
            if (batch.isEmpty()) {
                break;
            }

            Map<Path, List<CentralizedLog>> grouped = groupByArchiveFile(batch, properties.getZoneOffset());
            for (Map.Entry<Path, List<CentralizedLog>> entry : grouped.entrySet()) {
                appendLogs(entry.getKey(), entry.getValue());
                filesTouched++;
            }

            List<String> ids = batch.stream().map(CentralizedLog::getId).toList();
            Query deleteQuery = new Query(Criteria.where("_id").in(ids));
            totalDeleted += Math.toIntExact(mongoTemplate.remove(deleteQuery, CentralizedLog.class).getDeletedCount());
            totalArchived += batch.size();
        }

        return ArchiveRunResponse.builder()
                .cutoffTimestamp(cutoff)
                .archivedCount(totalArchived)
                .deletedCount(totalDeleted)
                .archiveFileCount(filesTouched)
                .build();
    }

    private Map<Path, List<CentralizedLog>> groupByArchiveFile(List<CentralizedLog> logs, ZoneOffset zoneOffset) {
        Map<Path, List<CentralizedLog>> grouped = new LinkedHashMap<>();
        for (CentralizedLog logEntry : logs) {
            LocalDate date = logEntry.getTimestamp().atOffset(zoneOffset).toLocalDate();
            String day = DAY_FORMATTER.format(date);
            Path path = Path.of(properties.getDirectory(), day + ".jsonl");
            grouped.computeIfAbsent(path, ignored -> new ArrayList<>()).add(logEntry);
        }
        return grouped;
    }

    private void appendLogs(Path archiveFile, List<CentralizedLog> logs) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                archiveFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND)) {
            for (CentralizedLog logEntry : logs) {
                writer.write(objectMapper.writeValueAsString(logEntry));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to archive logs to " + archiveFile, e);
        }
    }
}
