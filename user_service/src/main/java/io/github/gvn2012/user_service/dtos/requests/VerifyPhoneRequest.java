package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPhoneRequest {

    @NotBlank(message = "Verification code must not be blank")
    @Size(min = 4, max = 8, message = "Verification code must be 4-8 digits")
    private String code;
}
