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
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserEmergencyContactController {

    private final IUserEmergencyContactService emergencyContactService;

    @GetMapping("/{userId}/emergency-contacts")
    public ResponseEntity<APIResource<GetUserEmergencyContactResponse>> getUserEmergencyContacts(
            @PathVariable String userId) {
        APIResource<GetUserEmergencyContactResponse> response = emergencyContactService.getUserEmergencyContact(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{userId}/emergency-contacts")
    public ResponseEntity<APIResource<AddNewEmergencyContactResponse>> addNewEmergencyContact(
            @PathVariable String userId,
            @Valid @RequestBody AddNewEmergencyContactRequest request) {
        APIResource<AddNewEmergencyContactResponse> response = emergencyContactService.addNewEmergencyContact(
                UUID.fromString(userId),
                request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{userId}/emergency-contacts/{contactId}")
    public ResponseEntity<APIResource<UpdateEmergencyContactResponse>> updateEmergencyContact(
            @PathVariable String userId,
            @PathVariable String contactId,
            @Valid @RequestBody UpdateEmergencyContactRequest request) {
        APIResource<UpdateEmergencyContactResponse> response = emergencyContactService.updateEmergencyContact(
                UUID.fromString(userId),
                UUID.fromString(contactId),
                request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{userId}/emergency-contacts/{contactId}")
    public ResponseEntity<APIResource<DeleteEmergencyContactResponse>> deleteEmergencyContact(
            @PathVariable String userId,
            @PathVariable String contactId) {
        APIResource<DeleteEmergencyContactResponse> response = emergencyContactService.deleteEmergencyContact(
                UUID.fromString(userId),
                UUID.fromString(contactId));
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{userId}/emergency-contacts/{contactId}/set-primary")
    public ResponseEntity<APIResource<SetPrimaryEmergencyContactResponse>> setPrimaryEmergencyContact(
            @PathVariable String userId,
            @PathVariable String contactId) {
        APIResource<SetPrimaryEmergencyContactResponse> response = emergencyContactService.setPrimaryEmergencyContact(
                UUID.fromString(userId),
                UUID.fromString(contactId));
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
