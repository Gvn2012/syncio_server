package io.github.gvn2012.user_service.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {


    private final WebClient.Builder webClientBuilder;

    @GetMapping()
    public ResponseEntity<String> getUsers() {

        Object a = webClientBuilder.build().get().
                uri("http://auth-service/api/v1/auth/test").retrieve().bodyToMono(String.class).block();
        assert a != null;
        return ResponseEntity.ok(a.toString());
    }
}
