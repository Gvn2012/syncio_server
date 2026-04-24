package io.github.gvn2012.auth_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateLoginTokenResponse {
    private String accessToken;
    private String refreshToken;
    private List<String> userRoles;
}
