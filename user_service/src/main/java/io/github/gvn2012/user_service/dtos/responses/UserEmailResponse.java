package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserEmailResponse {
    private String id;
    private String email;
    private Boolean verified;
    private Boolean primary;
    private LocalDateTime verifiedAt;
    private EmailStatus status;
}
