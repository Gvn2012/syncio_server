package io.github.gvn2012.auth_service.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateLoginTokenResponse {
    private String accessToken;
    private String refreshToken;
}
