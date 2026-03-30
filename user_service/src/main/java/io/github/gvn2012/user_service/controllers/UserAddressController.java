package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewAddressRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateAddressRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.services.interfaces.IUserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserAddressController {

    private final IUserAddressService userAddressService;

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<APIResource<GetUserAddressResponse>> getUserAddresses(
            @PathVariable String userId) {
        APIResource<GetUserAddressResponse> response = userAddressService.getUserAddress(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<APIResource<AddNewAddressResponse>> addNewAddress(
            @PathVariable String userId,
            @Valid @RequestBody AddNewAddressRequest request) {
        APIResource<AddNewAddressResponse> response = userAddressService.addNewAddress(
                UUID.fromString(userId),
                request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<APIResource<UpdateAddressResponse>> updateAddress(
            @PathVariable String userId,
            @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        APIResource<UpdateAddressResponse> response = userAddressService.updateAddress(
                UUID.fromString(userId),
                UUID.fromString(addressId),
                request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<APIResource<DeleteAddressResponse>> deleteAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        APIResource<DeleteAddressResponse> response = userAddressService.deleteAddress(
                UUID.fromString(userId),
                UUID.fromString(addressId));
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping("/{userId}/addresses/{addressId}/set-primary")
    public ResponseEntity<APIResource<SetPrimaryAddressResponse>> setPrimaryAddress(
            @PathVariable String userId,
            @PathVariable String addressId) {
        APIResource<SetPrimaryAddressResponse> response = userAddressService.setPrimaryAddress(
                UUID.fromString(userId),
                UUID.fromString(addressId));
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
