package io.github.gvn2012.logging_service.dtos;

import io.github.gvn2012.logging_service.entities.CentralizedLog;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ArchivedLogSearchResponse {
    private List<CentralizedLog> items;
    private long scannedFiles;
    private long matchedItems;
}
