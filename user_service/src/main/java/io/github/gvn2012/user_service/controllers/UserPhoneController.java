package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.responses.GetUserPhoneResponse;
import io.github.gvn2012.user_service.dtos.requests.AddNewPhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdatePhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPhoneRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.services.interfaces.IUserPhoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserPhoneController {

        private final IUserPhoneService userPhoneService;

        @GetMapping("/{userId}/phones")
        public ResponseEntity<APIResource<GetUserPhoneResponse>> getUserPhones(
                        @PathVariable String userId) {
                APIResource<GetUserPhoneResponse> response = userPhoneService.getUserPhone(userId);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PostMapping("/{userId}/phones")
        public ResponseEntity<APIResource<AddNewPhoneResponse>> addNewPhone(
                        @PathVariable String userId,
                        @Valid @RequestBody AddNewPhoneRequest request) {
                APIResource<AddNewPhoneResponse> response = userPhoneService.addNewPhone(
                                UUID.fromString(userId),
                                request);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PatchMapping("/{userId}/phones/{phoneId}")
        public ResponseEntity<APIResource<UpdatePhoneResponse>> updatePhone(
                        @PathVariable String userId,
                        @PathVariable String phoneId,
                        @Valid @RequestBody UpdatePhoneRequest request) {
                APIResource<UpdatePhoneResponse> response = userPhoneService.updatePhone(
                                UUID.fromString(userId),
                                UUID.fromString(phoneId),
                                request);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PostMapping("/{userId}/phones/{phoneId}/verify")
        public ResponseEntity<APIResource<VerifyPhoneResponse>> verifyPhone(
                        @PathVariable String userId,
                        @PathVariable String phoneId,
                        @Valid @RequestBody VerifyPhoneRequest request) {
                APIResource<VerifyPhoneResponse> response = userPhoneService.verifyPhone(
                                UUID.fromString(userId),
                                UUID.fromString(phoneId),
                                request);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @DeleteMapping("/{userId}/phones/{phoneId}")
        public ResponseEntity<APIResource<DeletePhoneResponse>> deletePhone(
                        @PathVariable String userId,
                        @PathVariable String phoneId) {
                APIResource<DeletePhoneResponse> response = userPhoneService.deletePhone(
                                UUID.fromString(userId),
                                UUID.fromString(phoneId));
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PatchMapping("/{userId}/phones/{phoneId}/set-primary")
        public ResponseEntity<APIResource<SetPrimaryPhoneResponse>> setPrimaryPhone(
                        @PathVariable String userId,
                        @PathVariable String phoneId) {
                APIResource<SetPrimaryPhoneResponse> response = userPhoneService.setPrimaryPhone(
                                UUID.fromString(userId),
                                UUID.fromString(phoneId));
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PostMapping("/{userId}/phones/{phoneId}/resend-verification")
        public ResponseEntity<APIResource<Void>> resendVerificationCode(
                        @PathVariable String userId,
                        @PathVariable String phoneId) {
                APIResource<Void> response = userPhoneService.resendVerificationCode(
                                UUID.fromString(userId),
                                UUID.fromString(phoneId));
                return ResponseEntity.status(response.getStatus()).body(response);
        }
}