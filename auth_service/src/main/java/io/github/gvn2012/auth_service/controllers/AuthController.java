package io.github.gvn2012.auth_service.controllers;


import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.services.impls.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
