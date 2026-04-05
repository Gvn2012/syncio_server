package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartEmailVerificationResponse {
    private String emailVerificationId;
    private String email;
    private Integer resendAfterSeconds;
}
