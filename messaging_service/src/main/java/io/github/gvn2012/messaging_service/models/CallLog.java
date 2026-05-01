package io.github.gvn2012.messaging_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "call_logs")
public class CallLog {
    @Id
    private String callId;
    private String conversationId;
    private String initiatorId;
    private String callMode;
    private boolean isVoiceOnly;
    private long startedAt;
    private long endedAt;
    private int durationSeconds;
    private List<ParticipantEvent> participantEvents;
    private Map<String, Object> metadata;
}
