package io.github.gvn2012.user_service.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class LoginRequest {
    private String username;
    private String password;
}