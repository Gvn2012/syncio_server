package io.github.gvn2012.call_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEvent {
    private String userId;
    private String action;
    private long timestamp;
}
