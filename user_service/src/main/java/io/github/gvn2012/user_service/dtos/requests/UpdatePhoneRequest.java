package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePhoneRequest {

    @Size(max = 64)
    @Pattern(regexp = "^[0-9+\\-() ]+$", message = "Invalid phone number format")
    private String phoneNumber;

    @Pattern(regexp = "^(\\+?\\d{1,3}|\\d{1,4})$", message = "Invalid country code format")
    private String countryCode;

    private String phoneType;
}
