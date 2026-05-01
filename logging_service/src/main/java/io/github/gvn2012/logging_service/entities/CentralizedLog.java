package io.github.gvn2012.logging_service.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "centralized_logs")
@CompoundIndexes({
        @CompoundIndex(name = "service_timestamp_idx", def = "{'service': 1, 'timestamp': -1}"),
        @CompoundIndex(name = "user_action_idx", def = "{'userId': 1, 'action': 1, 'timestamp': -1}")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentralizedLog {

    @Id
    private String id;

    @Indexed
    private Instant timestamp;
    @Indexed
    private String severity;
    @Indexed
    private String service;
    private String environment;
    private String message;
    @Indexed
    private String traceId;
    @Indexed
    private String spanId;
    @Indexed
    private String requestId;
    @Indexed
    private String userId;
    @Indexed
    private String sessionId;
    @Indexed
    private String action;
    @Indexed
    private String outcome;
    private Map<String, Object> metadata;
}
