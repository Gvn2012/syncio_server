package io.github.gvn2012.post_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse implements Serializable {
    private UUID userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String avatarPath;
    private Boolean active;
    private Boolean suspended;
    private Boolean banned;
}
