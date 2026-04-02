package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

        private final IUserService userService;

        @PostMapping("/login")
        public ResponseEntity<APIResource<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request) {
                APIResource<LoginResponse> response = userService.login(request);
                return ResponseEntity
                                .status(response.getStatus())
                                .body(response);
        }

        @PostMapping("/register")
        public ResponseEntity<APIResource<UserRegisterResponse>> register(
                        @Valid @RequestBody UserRegisterRequest request) {
                APIResource<UserRegisterResponse> response = userService.register(request);
                return ResponseEntity
                                .status(response.getStatus())
                                .body(response);
        }

        @GetMapping("/{uid}")
        public ResponseEntity<APIResource<GetUserDetailResponse>> getUserDetails(
                        @Valid @PathVariable("uid") String userId) {
                APIResource<GetUserDetailResponse> response = userService.getUserDetail(userId);
                return ResponseEntity
                                .status(response.getStatus())
                                .body(response);
        }

        @GetMapping
        public ResponseEntity<APIResource<GetUserDetailResponse>> getUserDetailsByQuery(
                        @RequestParam String id) {
                APIResource<GetUserDetailResponse> response = userService.getUserDetail(id);
                return ResponseEntity
                                .status(response.getStatus())
                                .body(response);
        }

}
