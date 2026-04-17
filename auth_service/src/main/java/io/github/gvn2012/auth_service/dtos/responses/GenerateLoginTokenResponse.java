package io.github.gvn2012.auth_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateLoginTokenResponse {
    @JsonIgnore
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private List<String> userRoles;
}
