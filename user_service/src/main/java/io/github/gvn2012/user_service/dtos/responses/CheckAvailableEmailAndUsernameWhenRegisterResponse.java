package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckAvailableEmailAndUsernameWhenRegisterResponse {
    private Boolean isEmailAvailable;
    private Boolean isUsernameAvailable;
}
