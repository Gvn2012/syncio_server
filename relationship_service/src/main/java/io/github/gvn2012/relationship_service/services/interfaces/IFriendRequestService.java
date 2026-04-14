package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.requests.PendingRequestDirection;
import io.github.gvn2012.relationship_service.dtos.responses.PageResponse;
import io.github.gvn2012.relationship_service.dtos.responses.PendingFriendRequestResponse;

import java.util.UUID;

public interface IFriendRequestService {
    APIResource<Void> sendFriendRequest(UUID senderId, UUID receiverId, String message);
    APIResource<Void> acceptFriendRequest(UUID requestId, UUID userId);
    APIResource<Void> declineFriendRequest(UUID requestId, UUID userId);
    APIResource<Void> cancelFriendRequest(UUID requestId, UUID userId);
    APIResource<PageResponse<PendingFriendRequestResponse>> getPendingFriendRequests(
            UUID userId, PendingRequestDirection direction, int page, int size);
}
