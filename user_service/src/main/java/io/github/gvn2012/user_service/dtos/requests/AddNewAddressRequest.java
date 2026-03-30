package io.github.gvn2012.user_service.dtos.requests;

import io.github.gvn2012.user_service.entities.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddNewAddressRequest {

    @NotNull(message = "Address type must not be null")
    private AddressType addressType;

    @NotBlank(message = "Address line 1 must not be blank")
    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @NotBlank(message = "City must not be blank")
    @Size(max = 128)
    private String city;

    @Size(max = 128)
    private String state;

    @Size(max = 32)
    private String postalCode;

    @NotBlank(message = "Country must not be blank")
    @Size(max = 64)
    private String country;
}
