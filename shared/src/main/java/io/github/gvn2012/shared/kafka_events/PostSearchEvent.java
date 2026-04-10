package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchEvent {
    public enum OperationType { UPSERT, DELETE }

    private UUID postId;
    private UUID authorId;
    private String content;
    private LocalDateTime publishedAt;
    private String status;
    private OperationType operationType;
}
