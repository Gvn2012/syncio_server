package io.github.gvn2012.user_service.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data

public class LoginResponse {
    String accessToken;
    String refreshToken;
    String userId;
    String userRole;
}
