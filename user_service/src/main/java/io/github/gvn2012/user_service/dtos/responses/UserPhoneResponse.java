package io.github.gvn2012.user_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserPhoneResponse {
    private String id;
    private String phoneNumber;
    private Boolean verified;
    private Boolean primary;
    private LocalDateTime verifiedAt;
    private String metadata;
}
