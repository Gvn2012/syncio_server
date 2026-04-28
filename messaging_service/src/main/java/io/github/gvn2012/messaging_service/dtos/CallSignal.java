package io.github.gvn2012.messaging_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallSignal {
    private String type;
    private String senderId;
    private String recipientId;
    private Object payload;
}
