package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyPendingEmailResponse {
    private String emailVerificationId;
    private String email;
    private Boolean verified;
}
