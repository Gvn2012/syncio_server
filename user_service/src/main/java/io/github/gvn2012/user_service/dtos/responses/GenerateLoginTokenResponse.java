package io.github.gvn2012.user_service.dtos.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateLoginTokenResponse {
    private String accessToken;
    private String refreshToken;
    private List<String> userRoles;
}
