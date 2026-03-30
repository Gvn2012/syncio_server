package io.github.gvn2012.auth_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateResponse {
    private Boolean isValid = true;
    private String errorMessage = null;
    private String userId;
    private String userRole;
}
