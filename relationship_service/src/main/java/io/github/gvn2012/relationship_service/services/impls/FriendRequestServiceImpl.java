package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.clients.UserProfileClient;
import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.requests.PendingRequestDirection;
import io.github.gvn2012.relationship_service.dtos.responses.PageResponse;
import io.github.gvn2012.relationship_service.dtos.responses.PendingFriendRequestResponse;
import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import io.github.gvn2012.relationship_service.entities.FriendRequest;
import io.github.gvn2012.relationship_service.entities.UserFollow;
import io.github.gvn2012.relationship_service.entities.UserFriend;
import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.repositories.FriendRequestRepository;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserFollowRepository;
import io.github.gvn2012.relationship_service.repositories.UserFriendRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IFriendRequestService;
import io.github.gvn2012.relationship_service.services.kafka.RelationshipEventProducer;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements IFriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserFriendRepository friendRepository;
    private final UserFollowRepository followRepository;
    private final UserBlockRepository blockRepository;
    private final RelationshipEventProducer eventProducer;
    private final UserProfileClient userProfileClient;

    @Override
    @Transactional
    public APIResource<Void> sendFriendRequest(UUID senderId, UUID receiverId, String message) {
        if (senderId.equals(receiverId)) {
            return APIResource.error("SELF_FRIEND", "Cannot send friend request to yourself", HttpStatus.BAD_REQUEST,
                    null);
        }

        if (isBlockedEitherDirection(senderId, receiverId)) {
            return APIResource.error("BLOCKED", "Cannot send friend request due to blocking relationship",
                    HttpStatus.BAD_REQUEST, null);
        }

        if (findFriendship(senderId, receiverId)
                .map(friend -> friend.getStatus() == RelationshipStatus.ACTIVE)
                .orElse(false)) {
            return APIResource.error("ALREADY_FRIEND", "You are already friends with this user",
                    HttpStatus.BAD_REQUEST, null);
        }

        if (friendRequestRepository.existsBySenderUserIdAndReceiverUserIdAndStatus(
                senderId, receiverId, FriendRequestStatus.PENDING)) {
            return APIResource.error("ALREADY_PENDING", "Friend request already pending from you",
                    HttpStatus.BAD_REQUEST, null);
        }

        if (friendRequestRepository.existsBySenderUserIdAndReceiverUserIdAndStatus(
                receiverId, senderId, FriendRequestStatus.PENDING)) {
            return APIResource.error("REVERSE_PENDING", "This user has already sent you a friend request",
                    HttpStatus.BAD_REQUEST, null);
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
    public APIResource<Void> acceptFriendRequest(UUID requestId, UUID userId) {
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request == null || !userId.equals(request.getReceiverUserId())) {
            return APIResource.error("NOT_FOUND", "Friend request not found", HttpStatus.NOT_FOUND, null);
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return APIResource.error("INVALID_STATUS", "Friend request is not pending", HttpStatus.BAD_REQUEST, null);
        }

        if (isBlockedEitherDirection(request.getSenderUserId(), request.getReceiverUserId())) {
            return APIResource.error("BLOCKED", "Cannot accept friend request due to blocking relationship",
                    HttpStatus.BAD_REQUEST, null);
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        createOrReactivateFriendship(request.getSenderUserId(), request.getReceiverUserId());
        createOrReactivateFollow(request.getSenderUserId(), request.getReceiverUserId());
        createOrReactivateFollow(request.getReceiverUserId(), request.getSenderUserId());

        eventProducer.publishEvent(new RelationshipChangedEvent(request.getSenderUserId(), request.getReceiverUserId(),
                RelationshipChangedEvent.ChangeType.FRIEND_REQUEST_ACCEPTED));
        eventProducer.publishEvent(new RelationshipChangedEvent(request.getSenderUserId(), request.getReceiverUserId(),
                RelationshipChangedEvent.ChangeType.FOLLOW));
        eventProducer.publishEvent(new RelationshipChangedEvent(request.getReceiverUserId(), request.getSenderUserId(),
                RelationshipChangedEvent.ChangeType.FOLLOW));

        return APIResource.message("Friend request accepted", HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<Void> declineFriendRequest(UUID requestId, UUID userId) {
        FriendRequest request = friendRequestRepository.findById(requestId).orElse(null);
        if (request == null || !userId.equals(request.getReceiverUserId())) {
            return APIResource.error("NOT_FOUND", "Friend request not found", HttpStatus.NOT_FOUND, null);
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return APIResource.error("INVALID_STATUS", "Friend request is already processed", HttpStatus.BAD_REQUEST,
                    null);
        }

        request.setStatus(FriendRequestStatus.DECLINED);
        friendRequestRepository.save(request);

        return APIResource.message("Friend request declined", HttpStatus.OK);
    }

    @Override
    public APIResource<PageResponse<PendingFriendRequestResponse>> getPendingFriendRequests(
            UUID userId, PendingRequestDirection direction, int page, int size) {
        PendingRequestDirection safeDirection = direction != null ? direction : PendingRequestDirection.ALL;
        Pageable pageable = PageRequest.of(Math.max(page, 0), size > 0 ? Math.min(size, 100) : 20,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<FriendRequest> requests = switch (safeDirection) {
            case SENT -> friendRequestRepository.findAllBySenderUserIdAndStatus(
                    userId, FriendRequestStatus.PENDING, pageable);
            case RECEIVED -> friendRequestRepository.findAllByReceiverUserIdAndStatus(
                    userId, FriendRequestStatus.PENDING, pageable);
            case ALL -> friendRequestRepository.findAllPendingByUserId(
                    userId, FriendRequestStatus.PENDING, pageable);
        };

        List<UUID> otherUserIds = requests.getContent().stream()
                .map(request -> request.getSenderUserId().equals(userId)
                        ? request.getReceiverUserId()
                        : request.getSenderUserId())
                .toList();

        log.info("Fetching profiles for {} pending friend requests for user {}", otherUserIds.size(), userId);
        Map<UUID, UserProfileSummary> profiles = userProfileClient.getUserProfiles(otherUserIds);

        List<PendingFriendRequestResponse> content = requests.getContent().stream()
                .map(request -> {
                    UUID otherUserId = request.getSenderUserId().equals(userId)
                            ? request.getReceiverUserId()
                            : request.getSenderUserId();
                    
                    UserProfileSummary profile = profiles.get(otherUserId);
                    String username = null;
                    String displayName = "Unknown User";
                    String profilePictureUrl = null;

                    if (profile != null) {
                        username = profile.getUsername();
                        displayName = profile.getDisplayName();
                        profilePictureUrl = profile.getProfilePictureUrl();
                    }

                    if (profilePictureUrl == null) {
                        profilePictureUrl = generateFallbackAvatar(displayName);
                    }

                    return PendingFriendRequestResponse.builder()
                            .requestId(request.getId())
                            .senderUserId(request.getSenderUserId())
                            .receiverUserId(request.getReceiverUserId())
                            .otherUserId(otherUserId)
                            .direction(request.getSenderUserId().equals(userId) ? "SENT" : "RECEIVED")
                            .username(username)
                            .displayName(displayName)
                            .profilePictureUrl(profilePictureUrl)
                            .message(request.getMessage())
                            .seen(request.getIsSeen())
                            .createdAt(request.getCreatedAt() == null ? null
                                     : request.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                            .build();
                })
                .toList();

        return APIResource.ok("Pending friend requests retrieved", PageResponse.<PendingFriendRequestResponse>builder()
                .content(content)
                .page(requests.getNumber())
                .size(requests.getSize())
                .totalElements(requests.getTotalElements())
                .totalPages(requests.getTotalPages())
                .hasNext(requests.hasNext())
                .hasPrevious(requests.hasPrevious())
                .build());
    }

    private String generateFallbackAvatar(String name) {
        String initial = "U";
        if (org.springframework.util.StringUtils.hasText(name)) {
            initial = name.substring(0, 1).toUpperCase();
        }
        return String.format("https://ui-avatars.com/api/?name=%s&background=random&color=fff", initial);
    }

    private boolean isBlockedEitherDirection(UUID userA, UUID userB) {
        return blockRepository.existsByBlockerUserIdAndBlockedUserId(userA, userB)
                || blockRepository.existsByBlockerUserIdAndBlockedUserId(userB, userA);
    }

    private java.util.Optional<UserFriend> findFriendship(UUID userA, UUID userB) {
        UUID first = userA.compareTo(userB) < 0 ? userA : userB;
        UUID second = userA.compareTo(userB) < 0 ? userB : userA;
        return friendRepository.findByUser1IdAndUser2Id(first, second);
    }

    private void createOrReactivateFriendship(UUID userA, UUID userB) {
        UUID first = userA.compareTo(userB) < 0 ? userA : userB;
        UUID second = userA.compareTo(userB) < 0 ? userB : userA;

        UserFriend friend = friendRepository.findByUser1IdAndUser2Id(first, second).orElse(null);
        if (friend == null) {
            friend = new UserFriend();
            friend.setUser1Id(first);
            friend.setUser2Id(second);
        }

        friend.setStatus(RelationshipStatus.ACTIVE);
        friend.setAcceptedAt(LocalDateTime.now());
        friend.setInitiatedByUserId(userA);
        friendRepository.save(friend);
    }

    private void createOrReactivateFollow(UUID followerId, UUID followeeId) {
        UserFollow follow = followRepository.findByFollowerUserIdAndFolloweeUserId(followerId, followeeId).orElse(null);
        if (follow == null) {
            follow = new UserFollow();
            follow.setFollowerUserId(followerId);
            follow.setFolloweeUserId(followeeId);
        }

        follow.setStatus(RelationshipStatus.ACTIVE);
        followRepository.save(follow);
    }
}
