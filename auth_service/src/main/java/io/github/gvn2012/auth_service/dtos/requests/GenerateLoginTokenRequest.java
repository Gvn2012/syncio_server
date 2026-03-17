package io.github.gvn2012.auth_service.dtos.requests;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateLoginTokenRequest {
    private String username;
    private String userId;
}
