package io.github.gvn2012.auth_service.controllers;


import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.services.impls.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody GenerateLoginTokenRequest request
    ) {
        APIResource<GenerateLoginTokenResponse> response = authService.generateLoginToken(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<APIResource<String>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        log.info("Auth Header: {}", authorizationHeader);
        boolean isValid = authService.validateToken(authorizationHeader);
        if (isValid) {
            return ResponseEntity.ok(APIResource.ok("Token is valid", "VALID"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResource.error("UNAUTHORIZED", "Invalid or expired token", HttpStatus.UNAUTHORIZED, null));
        }
    }

}
