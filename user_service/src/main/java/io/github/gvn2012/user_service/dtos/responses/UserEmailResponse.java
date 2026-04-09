package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEmailResponse {
    private String id;
    private String email;
    private Boolean verified;
    private Boolean primary;
    private LocalDateTime verifiedAt;
    private EmailStatus status;
    private Map<String, Object> metadata;
}
