package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmergencyContactRequest {

    @Size(max = 128)
    private String contactName;

    @Size(max = 64)
    private String relationship;

    @Size(max = 64)
    private String phoneNumber;

    @Email(message = "Valid email is required if provided")
    @Size(max = 255)
    private String email;

    @Min(1)
    @Max(10)
    private Integer priority;
}
