package io.github.gvn2012.messaging_service.models;

import io.github.gvn2012.messaging_service.models.enums.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "CallSession", timeToLive = 7200)
public class CallSession {
    @Id
    private String callId;
    private String conversationId;
    private String initiatorId;
    private String callMode;
    private long startedAt;
    private CallStatus status;
    @Builder.Default
    private Set<String> activeParticipantIds = new HashSet<>();
    @Builder.Default
    private List<ParticipantEvent> participantEvents = new ArrayList<>();
}
