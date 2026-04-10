package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchEvent {
    public enum OperationType { UPSERT, DELETE }

    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private OperationType operationType;
}
