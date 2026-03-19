package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.AddNewEmailResponse;
import io.github.gvn2012.user_service.services.impls.UserEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserEmailController {

    private final UserEmailService userEmailService;

    @PostMapping("/{userId}/emails")
    public ResponseEntity<APIResource<AddNewEmailResponse>> addNewEmail(
            @PathVariable String userId,
            @Valid @RequestBody AddNewEmailRequest request
    ) {
        APIResource<AddNewEmailResponse> response =
                userEmailService.addNewEmail(UUID.fromString(userId), request);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
