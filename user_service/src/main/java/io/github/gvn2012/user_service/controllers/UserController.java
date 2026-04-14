package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateProfilePictureRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.CheckAvailableEmailAndUsernameWhenRegisterResponse;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;
import io.github.gvn2012.user_service.services.interfaces.IUserProfilePictureService;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IUserProfilePictureService userProfilePictureService;

    @PostMapping("/login")
    public ResponseEntity<APIResource<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        APIResource<LoginResponse> response = userService.login(request);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<APIResource<UserRegisterResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {
        APIResource<UserRegisterResponse> response = userService.register(request);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{uid}")
    public ResponseEntity<APIResource<GetUserDetailResponse>> getUserDetails(
            @Valid @PathVariable("uid") String userId) {
        APIResource<GetUserDetailResponse> response = userService.getUserDetail(userId);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    public ResponseEntity<APIResource<GetUserDetailResponse>> getUserDetailsByQuery(
            @RequestParam String id) {
        APIResource<GetUserDetailResponse> response = userService.getUserDetail(id);
        HttpStatus status = response.getStatus() != null ? response.getStatus() : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<APIResource<Map<UUID, GetUserDetailResponse>>> getUsersDetailByBatch(
            @RequestBody Set<UUID> userIds) {
        APIResource<Map<UUID, GetUserDetailResponse>> response = userService.getUsersDetail(userIds);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/batch/summaries")
    public ResponseEntity<APIResource<Map<UUID, io.github.gvn2012.user_service.dtos.responses.UserSummaryResponse>>> getUsersSummary(
            @RequestBody Set<UUID> userIds) {
        APIResource<Map<UUID, io.github.gvn2012.user_service.dtos.responses.UserSummaryResponse>> response = userService.getUsersSummary(userIds);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
 
    @GetMapping("/check-username-email-availability")
    public ResponseEntity<APIResource<CheckAvailableEmailAndUsernameWhenRegisterResponse>> checkUsernameEmailAvailability(
            @RequestParam String email, @RequestParam String username) {
        APIResource<CheckAvailableEmailAndUsernameWhenRegisterResponse> response = userService
                .checkAvailableEmailAndUsernameWhenRegister(email, username);
        org.springframework.http.HttpStatusCode status = response.getStatus() != null ? response.getStatus()
                : org.springframework.http.HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/{uid}/profile-picture")
    public ResponseEntity<APIResource<Void>> updateProfilePicture(
            @PathVariable("uid") UUID userId,
            @Valid @RequestBody UpdateProfilePictureRequest request) {
        APIResource<Void> response = userProfilePictureService.updateProfilePicture(userId, request);
        HttpStatus status = response.getStatus() != null ? response.getStatus() : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
