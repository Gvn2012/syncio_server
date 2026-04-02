package io.github.gvn2012.relationship_service.dtos.responses;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckBlockStatusResponse {
    private Boolean isBlocked;
    private Boolean isBidirectionalBlocked;

    @Builder.Default
    private UUID blockerId = null;
}
