package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactorSummaryResponse {
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private boolean isFriend;
    private boolean isBlocked;
}
