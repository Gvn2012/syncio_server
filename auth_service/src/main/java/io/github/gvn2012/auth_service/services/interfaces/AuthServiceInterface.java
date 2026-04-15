package io.github.gvn2012.auth_service.services.interfaces;

import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.dtos.responses.ValidateResponse;

import io.github.gvn2012.auth_service.dtos.requests.LogoutRequest;
import io.github.gvn2012.auth_service.dtos.requests.RefreshTokenRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthServiceInterface {

    APIResource<GenerateLoginTokenResponse> generateLoginToken(GenerateLoginTokenRequest request, HttpServletRequest httpRequest);

    APIResource<GenerateLoginTokenResponse> refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest);

    APIResource<String> logout(LogoutRequest request);

    APIResource<String> forceLogout(String userId);

    ValidateResponse validateToken(String token);
}
