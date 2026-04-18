package io.github.gvn2012.auth_service.dtos.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateLoginTokenResponse {
    private String accessToken;
    private String refreshToken;
    private List<String> userRoles;
}
