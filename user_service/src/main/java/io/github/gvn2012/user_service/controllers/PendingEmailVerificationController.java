package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.StartEmailVerificationRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPendingEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.StartEmailVerificationResponse;
import io.github.gvn2012.user_service.dtos.responses.VerifyPendingEmailResponse;
import io.github.gvn2012.user_service.services.interfaces.IPendingEmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/email-verifications")
@RequiredArgsConstructor
public class PendingEmailVerificationController {

    private final IPendingEmailVerificationService pendingEmailVerificationService;

    @PostMapping
    public ResponseEntity<APIResource<StartEmailVerificationResponse>> start(
            @Valid @RequestBody StartEmailVerificationRequest request) {
        APIResource<StartEmailVerificationResponse> response = pendingEmailVerificationService.start(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // aa

    @PostMapping("/{verificationId}/verify")
    public ResponseEntity<APIResource<VerifyPendingEmailResponse>> verify(
            @PathVariable UUID verificationId,
            @Valid @RequestBody VerifyPendingEmailRequest request) {
        APIResource<VerifyPendingEmailResponse> response = pendingEmailVerificationService.verify(verificationId,
                request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{verificationId}/resend")
    public ResponseEntity<APIResource<Void>> resend(@PathVariable UUID verificationId) {
        APIResource<Void> response = pendingEmailVerificationService.resend(verificationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
