package io.github.gvn2012.auth_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateLoginTokenResponse {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String accessToken;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String refreshToken;
    private List<String> userRoles;
}
