package io.github.gvn2012.user_service.dtos.responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data

public class LoginResponse {
    String accessToken;
    String refreshToken;
    String userId;
    List<String> userRoles;
}
