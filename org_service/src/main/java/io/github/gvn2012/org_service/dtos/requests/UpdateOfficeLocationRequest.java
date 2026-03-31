package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOfficeLocationRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @Size(max = 128, message = "City must not exceed 128 characters")
    private String city;

    @Size(max = 128, message = "State must not exceed 128 characters")
    private String state;

    @Size(max = 64, message = "Country must not exceed 64 characters")
    private String country;

    @Size(max = 32, message = "Postal code must not exceed 32 characters")
    private String postalCode;

    @Size(max = 64, message = "Phone number must not exceed 64 characters")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private Boolean headquarters;

    private Integer capacity;

    private OfficeStatus status;
}
