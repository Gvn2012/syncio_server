package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.DeleteEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.AddNewEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.DeleteEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.UpdateEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.VerifyEmailResponse;
import io.github.gvn2012.user_service.services.impls.UserEmailServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserEmailController {

    private final UserEmailServiceImpl userEmailServiceImpl;

    @PostMapping("/{userId}/emails")
    public ResponseEntity<APIResource<AddNewEmailResponse>> addNewEmail(
            @PathVariable String userId,
            @Valid @RequestBody AddNewEmailRequest request
    ) {
        APIResource<AddNewEmailResponse> response =
                userEmailServiceImpl.addNewEmail(UUID.fromString(userId), request);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @PatchMapping("/{userId}/emails/{emailId}")
    public ResponseEntity<APIResource<UpdateEmailResponse>> updateEmail(
            @PathVariable String userId,
            @PathVariable String emailId,
            @Valid @RequestBody UpdateEmailRequest request
            ) {
        APIResource<UpdateEmailResponse> response = userEmailServiceImpl.updateEmail(
                UUID.fromString(userId),
                UUID.fromString(emailId),
                request
        );

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @GetMapping("/emails/verify")
    public ResponseEntity<APIResource<VerifyEmailResponse>> verifyEmail(
            @RequestParam UUID emailId,
            @RequestParam UUID userId,
            @RequestParam String token,
            @RequestBody VerifyEmailRequest request
    ) {
        APIResource<VerifyEmailResponse> response = userEmailServiceImpl.verifyEmail(emailId, userId, token, request);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @DeleteMapping("/{userId}/emails/{emailId}")
    public ResponseEntity<APIResource<DeleteEmailResponse>> deleteEmail(
            @PathVariable String userId,
            @PathVariable String emailId,
            @RequestBody DeleteEmailRequest request
    ) {
        APIResource<DeleteEmailResponse> response = userEmailServiceImpl.deleteEmail(
                UUID.fromString(userId),
                UUID.fromString(emailId),
                request
        );

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
