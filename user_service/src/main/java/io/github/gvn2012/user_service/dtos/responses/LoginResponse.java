package io.github.gvn2012.user_service.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginResponse {
    @JsonIgnore
    String accessToken;
    @JsonIgnore
    String refreshToken;
    String userId;
    List<String> userRoles;
    String orgId;
}
