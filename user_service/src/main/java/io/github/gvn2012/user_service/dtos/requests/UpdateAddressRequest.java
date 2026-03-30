package io.github.gvn2012.user_service.dtos.requests;

import io.github.gvn2012.user_service.entities.enums.AddressType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAddressRequest {

    private AddressType addressType;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 128)
    private String city;

    @Size(max = 128)
    private String state;

    @Size(max = 32)
    private String postalCode;

    @Size(max = 64)
    private String country;
}
