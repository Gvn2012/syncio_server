package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import java.util.UUID;

public interface IFriendRequestService {
    APIResource<Void> sendFriendRequest(UUID senderId, UUID receiverId, String message);
    APIResource<Void> acceptFriendRequest(UUID requestId, UUID userId);
    APIResource<Void> declineFriendRequest(UUID requestId, UUID userId);
}
