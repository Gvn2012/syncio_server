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
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/generate-tokens")
    public ResponseEntity<APIResource<GenerateLoginTokenResponse>> generateToken(
            @RequestBody GenerateLoginTokenRequest request, HttpServletRequest httpRequest) {
        log.info("Generate Token Request: {}", request);
        APIResource<GenerateLoginTokenResponse> response = authService.generateLoginToken(request, httpRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<APIResource<ValidateResponse>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        log.info("Auth Header: {}", authorizationHeader);
        ValidateResponse validation = authService.validateToken(authorizationHeader);

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
            @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        log.info("Refresh Token Request");
        APIResource<GenerateLoginTokenResponse> response = authService.refreshToken(request, httpRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResource<String>> logout(
            @RequestBody LogoutRequest request) {
        log.info("Logout Request");
        APIResource<String> response = authService.logout(request);
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
