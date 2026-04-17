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

    @SuppressWarnings("null")
    @PostMapping("/generate-tokens")
    public ResponseEntity<APIResource<GenerateLoginTokenResponse>> generateToken(
            @RequestBody GenerateLoginTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("Generate Token Request: {}", request);
        APIResource<GenerateLoginTokenResponse> response = authService.generateLoginToken(request, httpRequest);

        if (response.isSuccess() && response.getData() != null) {
            setTokenCookies(httpResponse, response.getData().getAccessToken(), response.getData().getRefreshToken());
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
            token = getCookieValue(request, "accessToken");
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

    @SuppressWarnings("null")
    @PostMapping("/refresh")
    public ResponseEntity<APIResource<GenerateLoginTokenResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("Refresh Token Request");

        String refreshToken = (request != null) ? request.getRefreshToken() : null;
        if (refreshToken == null || refreshToken.isBlank()) {
            refreshToken = getCookieValue(httpRequest, "refreshToken");
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResource.error("UNAUTHORIZED", "Refresh token is missing", HttpStatus.UNAUTHORIZED, null));
        }

        // Create a temporary request object if needed for the service
        RefreshTokenRequest serviceRequest = new RefreshTokenRequest();
        serviceRequest.setRefreshToken(refreshToken);

        APIResource<GenerateLoginTokenResponse> response = authService.refreshToken(serviceRequest, httpRequest);

        if (response.isSuccess() && response.getData() != null) {
            setTokenCookies(httpResponse, response.getData().getAccessToken(), response.getData().getRefreshToken());
        }

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @SuppressWarnings("null")
    @PostMapping("/logout")
    public ResponseEntity<APIResource<String>> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletResponse httpResponse) {
        log.info("Logout Request");
        APIResource<String> response = authService.logout(request);

        clearTokenCookies(httpResponse);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600);

        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(30 * 24 * 3600);

        response.addCookie(refreshCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("null")
    @PostMapping("/force-logout/{userId}")
    public ResponseEntity<APIResource<String>> forceLogout(
            @PathVariable String userId) {
        log.info("Force Logout Request for User: {}", userId);
        APIResource<String> response = authService.forceLogout(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
