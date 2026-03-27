package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneDto {
    private String id;
    private String countryCode;
    private String phoneNumber;
    private Boolean verified;
    private Boolean primary;
}