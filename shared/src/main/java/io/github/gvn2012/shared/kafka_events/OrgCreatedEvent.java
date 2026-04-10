package io.github.gvn2012.shared.kafka_events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgCreatedEvent implements Serializable {
    private String eventId;
    private String orgId;
    private String ownerId;
    private String name;
    private String email;
}
