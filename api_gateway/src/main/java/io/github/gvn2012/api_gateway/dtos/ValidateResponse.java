package io.github.gvn2012.api_gateway.dtos;

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
