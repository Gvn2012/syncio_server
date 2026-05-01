package io.github.gvn2012.logging_service.dtos;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ArchiveRunResponse {
    private Instant cutoffTimestamp;
    private int archivedCount;
    private int deletedCount;
    private int archiveFileCount;
}
