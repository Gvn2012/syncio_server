package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsOutboundEvent {
    private String type;
    private String destination;
    private List<String> recipientIds;
    private boolean broadcast;
    private String payload;
}
