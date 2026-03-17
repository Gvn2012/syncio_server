package io.github.gvn2012.user_service.controllers;

import com.netflix.discovery.converters.Auto;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.services.impls.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final WebClient.Builder webClientBuilder;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<APIResource<LoginResponse>> login(
            @RequestBody LoginRequest request
    ) {
        APIResource<LoginResponse> response = userService.login(request);
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

}
