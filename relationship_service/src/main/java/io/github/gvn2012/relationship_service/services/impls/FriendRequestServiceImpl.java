package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.entities.FriendRequest;
import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
import io.github.gvn2012.relationship_service.repositories.FriendRequestRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IFriendRequestService;
import io.github.gvn2012.relationship_service.services.kafka.RelationshipEventProducer;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements IFriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRelationshipRepository relationshipRepository;
    private final RelationshipEventProducer eventProducer;

    @Override
    @Transactional
    public APIResource<Void> sendFriendRequest(UUID senderId, UUID receiverId, String message) {
        if (senderId.equals(receiverId)) {
            return APIResource.error("SELF_FRIEND", "Cannot send friend request to yourself", HttpStatus.BAD_REQUEST,
                    null);
        }

        if (friendRequestRepository.existsBySenderUserIdAndReceiverUserIdAndStatus(senderId, receiverId,
                FriendRequestStatus.PENDING)) {
            return APIResource.error("ALREADY_PENDING", "Friend request already pending", HttpStatus.BAD_REQUEST, null);
        }

        FriendRequest request = new FriendRequest();
        request.setSenderUserId(senderId);
        request.setReceiverUserId(receiverId);
        request.setMessage(message);
        request.setStatus(FriendRequestStatus.PENDING);
        friendRequestRepository.save(request);

        eventProducer.publishEvent(new RelationshipChangedEvent(senderId, receiverId,
                RelationshipChangedEvent.ChangeType.FRIEND_REQUEST_SENT));

        return APIResource.message("Friend request sent successfully", HttpStatus.CREATED);
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public APIResource<Void> acceptFriendRequest(UUID requestId, UUID userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElse(null);

        System.out.println("Friend requestId: " + requestId);
        System.out.println("Friend request found: " + request);
        if (request == null || request.getReceiverUserId() == null || !request.getReceiverUserId().equals(userId)) {
            return APIResource.error("NOT_FOUND", "Friend request not found", HttpStatus.NOT_FOUND, null);
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return APIResource.error("INVALID_STATUS", "Friend request is not pending", HttpStatus.BAD_REQUEST, null);
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Create bi-directional relationship
        createFriendRelationship(request.getSenderUserId(), request.getReceiverUserId());
        createFriendRelationship(request.getReceiverUserId(), request.getSenderUserId());

        @SuppressWarnings("null")
        RelationshipChangedEvent event = new RelationshipChangedEvent(request.getSenderUserId(),
                request.getReceiverUserId(), RelationshipChangedEvent.ChangeType.FRIEND_REQUEST_ACCEPTED);
        eventProducer.publishEvent(event);

        return APIResource.message("Friend request accepted", HttpStatus.OK);
    }

    private void createFriendRelationship(UUID source, UUID target) {
        UserRelationship rel = relationshipRepository.findBySourceUserIdAndTargetUserIdAndRelationshipType(
                source, target, RelationshipType.FRIEND).orElse(null);
        if (rel == null) {
            rel = new UserRelationship();
            rel.setSourceUserId(source);
            rel.setTargetUserId(target);
            rel.setRelationshipType(RelationshipType.FRIEND);
        }
        rel.setStatus(RelationshipStatus.ACTIVE);
        rel.setAcceptedAt(LocalDateTime.now());
        relationshipRepository.save(rel);
    }
}
