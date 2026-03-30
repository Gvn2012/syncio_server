package io.github.gvn2012.user_service.controllers;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.responses.GetUserPhoneResponse;
import io.github.gvn2012.user_service.services.impls.UserPhoneServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserPhoneController {

    private final UserPhoneServiceImpl userPhoneServiceImpl;

    @GetMapping("/{userId}/phone")
    public ResponseEntity<APIResource<GetUserPhoneResponse>> getUserPhones(
            @PathVariable String userId) {
        APIResource<GetUserPhoneResponse> response = userPhoneServiceImpl.getUserPhone(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
