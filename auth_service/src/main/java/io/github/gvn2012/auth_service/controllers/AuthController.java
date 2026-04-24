package io.github.gvn2012.auth_service.controllers;

import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.requests.LogoutRequest;
import io.github.gvn2012.auth_service.dtos.requests.RefreshTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.dtos.responses.ValidateResponse;
import io.github.gvn2012.auth_service.services.impls.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private void setCookies(HttpServletResponse response, GenerateLoginTokenResponse tokens) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 3600)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private void clearCookies(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    @PostMapping("/generate-tokens")
    public ResponseEntity<APIResource<GenerateLoginTokenResponse>> generateToken(
            @RequestBody GenerateLoginTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("Generate Token Request: {}", request);
        APIResource<GenerateLoginTokenResponse> response = authService.generateLoginToken(request, httpRequest);

        if (response.isSuccess() && response.getData() != null) {
            setCookies(httpResponse, response.getData());
        }

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<APIResource<ValidateResponse>> validateToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletRequest request) {
        log.info("Validating token...");
        String token = authorizationHeader;

        if (token == null || token.isBlank()) {
            if (request.getCookies() != null) {
                token = Arrays.stream(request.getCookies())
                        .filter(c -> "accessToken".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
        }

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResource.error("UNAUTHORIZED", "Token is missing", HttpStatus.UNAUTHORIZED, null));
        }

        ValidateResponse validation = authService.validateToken(token);

        if (validation.getIsValid()) {
            return ResponseEntity.status(HttpStatus.OK).body(APIResource.ok("Token is valid", validation));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResource.error("UNAUTHORIZED", validation.getErrorMessage(), HttpStatus.UNAUTHORIZED,
                            null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResource<GenerateLoginTokenResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("Refresh Token Request");

        String refreshToken = (request != null) ? request.getRefreshToken() : null;

        if (refreshToken == null || refreshToken.isBlank()) {
            if (httpRequest.getCookies() != null) {
                refreshToken = Arrays.stream(httpRequest.getCookies())
                        .filter(c -> "refreshToken".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResource.error("UNAUTHORIZED", "Refresh token is missing", HttpStatus.UNAUTHORIZED, null));
        }

        RefreshTokenRequest serviceRequest = new RefreshTokenRequest();
        serviceRequest.setRefreshToken(refreshToken);
        APIResource<GenerateLoginTokenResponse> response = authService.refreshToken(serviceRequest, httpRequest);

        if (response.isSuccess() && response.getData() != null) {
            setCookies(httpResponse, response.getData());
        }

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResource<String>> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletResponse httpResponse) {
        log.info("Logout Request");
        APIResource<String> response = authService.logout(request);
        clearCookies(httpResponse);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/force-logout/{userId}")
    public ResponseEntity<APIResource<String>> forceLogout(
            @PathVariable String userId) {
        log.info("Force Logout Request for User: {}", userId);
        APIResource<String> response = authService.forceLogout(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
