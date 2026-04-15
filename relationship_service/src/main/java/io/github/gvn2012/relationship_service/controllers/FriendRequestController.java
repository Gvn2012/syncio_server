package io.github.gvn2012.relationship_service.controllers;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.requests.PendingRequestDirection;
import io.github.gvn2012.relationship_service.dtos.requests.SendFriendRequest;
import io.github.gvn2012.relationship_service.dtos.responses.PageResponse;
import io.github.gvn2012.relationship_service.dtos.responses.PendingFriendRequestResponse;
import io.github.gvn2012.relationship_service.services.interfaces.IFriendRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatusCode;

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
                return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus().value()))
                                .body(response);
        }

        @PostMapping("/accept/{reqid}")
        public ResponseEntity<APIResource<Void>> acceptRequest(
                        @RequestHeader("X-User-Id") UUID userId,
                        @PathVariable("reqid") UUID requestId) {
                APIResource<Void> response = friendRequestService.acceptFriendRequest(requestId, userId);
                return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus().value()))
                                .body(response);
        }

        @PostMapping("/decline/{reqid}")
        public ResponseEntity<APIResource<Void>> declineRequest(
                        @RequestHeader("X-User-Id") UUID userId,
                        @PathVariable("reqid") UUID requestId) {
                APIResource<Void> response = friendRequestService.declineFriendRequest(requestId, userId);
                return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus().value()))
                                .body(response);
        }

        @PostMapping("/cancel/{reqid}")
        public ResponseEntity<APIResource<Void>> cancelRequest(
                        @RequestHeader("X-User-Id") UUID userId,
                        @PathVariable("reqid") UUID requestId) {
                APIResource<Void> response = friendRequestService.cancelFriendRequest(requestId, userId);
                return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus().value()))
                                .body(response);
        }

        @GetMapping("/pending")
        public ResponseEntity<APIResource<PageResponse<PendingFriendRequestResponse>>> getPendingRequests(
                        @RequestHeader("X-User-Id") UUID userId,
                        @RequestParam(defaultValue = "ALL") PendingRequestDirection direction,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                APIResource<PageResponse<PendingFriendRequestResponse>> response = friendRequestService
                                .getPendingFriendRequests(userId, direction, page, size);
                return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus().value()))
                                .body(response);
        }
}
