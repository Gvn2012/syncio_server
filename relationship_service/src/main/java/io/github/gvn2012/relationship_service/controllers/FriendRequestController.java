package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.requests.SendFriendRequest;
import io.github.gvn2012.relationship_service.services.interfaces.IFriendRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rs/friend-requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final IFriendRequestService friendRequestService;

    @PostMapping("/send")
    public ResponseEntity<APIResource<Void>> sendRequest(
            @RequestHeader("X-User-Id") UUID senderId,
            @Valid @RequestBody SendFriendRequest request) {
        APIResource<Void> response = friendRequestService.sendFriendRequest(
                senderId, request.getTargetUserId(), request.getMessage());
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @PostMapping("/accept/{reqid}")
    public ResponseEntity<APIResource<Void>> acceptRequest(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable("reqid") UUID requestId) {
        APIResource<Void> response = friendRequestService.acceptFriendRequest(requestId, userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }

    @PostMapping("/decline/{reqid}")
    public ResponseEntity<APIResource<Void>> declineRequest(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable("reqid") UUID requestId) {
        APIResource<Void> response = friendRequestService.declineFriendRequest(requestId, userId);
        return ResponseEntity.status(org.springframework.http.HttpStatusCode.valueOf(response.getStatus().value()))
                .body(response);
    }
}
