package io.github.gvn2012.call_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("GroupCallRoom")
public class GroupCallRoom {
    @Id
    private String conversationId;
    private String callMode;
    @Builder.Default
    private Set<String> activeParticipantIds = new HashSet<>();
}
