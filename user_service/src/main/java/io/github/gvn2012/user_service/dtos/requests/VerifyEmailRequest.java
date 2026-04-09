package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.Pattern;
import lombok.Data;



@Data
public class VerifyEmailRequest {

    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    private String code;

}
