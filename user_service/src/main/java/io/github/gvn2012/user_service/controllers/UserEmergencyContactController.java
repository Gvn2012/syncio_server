package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.services.interfaces.IUserEmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserEmergencyContactController {

        private final IUserEmergencyContactService emergencyContactService;

        @GetMapping("/{uid}/emergency-contacts")
        public ResponseEntity<APIResource<GetUserEmergencyContactResponse>> getUserEmergencyContacts(
                        @PathVariable("uid") String userId) {
                APIResource<GetUserEmergencyContactResponse> response = emergencyContactService
                                .getUserEmergencyContact(userId);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PostMapping("/{uid}/emergency-contacts")
        public ResponseEntity<APIResource<AddNewEmergencyContactResponse>> addNewEmergencyContact(
                        @PathVariable("uid") String userId,
                        @Valid @RequestBody AddNewEmergencyContactRequest request) {
                APIResource<AddNewEmergencyContactResponse> response = emergencyContactService.addNewEmergencyContact(
                                UUID.fromString(userId),
                                request);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PatchMapping("/{uid}/emergency-contacts/{cid}")
        public ResponseEntity<APIResource<UpdateEmergencyContactResponse>> updateEmergencyContact(
                        @PathVariable("uid") String userId,
                        @PathVariable("cid") String contactId,
                        @Valid @RequestBody UpdateEmergencyContactRequest request) {
                APIResource<UpdateEmergencyContactResponse> response = emergencyContactService.updateEmergencyContact(
                                UUID.fromString(userId),
                                UUID.fromString(contactId),
                                request);
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @DeleteMapping("/{uid}/emergency-contacts/{cid}")
        public ResponseEntity<APIResource<DeleteEmergencyContactResponse>> deleteEmergencyContact(
                        @PathVariable("uid") String userId,
                        @PathVariable("cid") String contactId) {
                APIResource<DeleteEmergencyContactResponse> response = emergencyContactService.deleteEmergencyContact(
                                UUID.fromString(userId),
                                UUID.fromString(contactId));
                return ResponseEntity.status(response.getStatus()).body(response);
        }

        @PatchMapping("/{uid}/emergency-contacts/{cid}/set-primary")
        public ResponseEntity<APIResource<SetPrimaryEmergencyContactResponse>> setPrimaryEmergencyContact(
                        @PathVariable("uid") String userId,
                        @PathVariable("cid") String contactId) {
                APIResource<SetPrimaryEmergencyContactResponse> response = emergencyContactService
                                .setPrimaryEmergencyContact(
                                                UUID.fromString(userId),
                                                UUID.fromString(contactId));
                return ResponseEntity.status(response.getStatus()).body(response);
        }
}
