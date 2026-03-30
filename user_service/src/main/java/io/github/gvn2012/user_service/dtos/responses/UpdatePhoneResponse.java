package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePhoneResponse {
    private String phoneId;
    private String oldPhoneNumber;
    private String newPhoneNumber;
}
