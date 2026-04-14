package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.clients.UserProfileClient;
import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.PageResponse;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipResponse;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipStatusResponse;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipUserSummaryResponse;
import io.github.gvn2012.relationship_service.dtos.responses.UserProfileSummary;
import io.github.gvn2012.relationship_service.entities.FriendRequest;
import io.github.gvn2012.relationship_service.entities.UserFollow;
import io.github.gvn2012.relationship_service.entities.UserFriend;
import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
import io.github.gvn2012.relationship_service.repositories.FriendRequestRepository;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserFollowRepository;
import io.github.gvn2012.relationship_service.repositories.UserFriendRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IRelationshipService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl implements IRelationshipService {

    private final UserFollowRepository followRepository;
    private final UserFriendRepository friendRepository;
    private final UserBlockRepository userBlockRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RelationshipEventProducer eventProducer;
    private final UserProfileClient userProfileClient;

    @Override
    @Transactional
    public APIResource<RelationshipResponse> follow(UUID sourceId, UUID targetId) {
        if (sourceId.equals(targetId)) {
            return APIResource.error("SELF_FOLLOW", "Cannot follow yourself", HttpStatus.BAD_REQUEST, null);
        }

        if (isBlockedEitherDirection(sourceId, targetId)) {
            return APIResource.error("BLOCKED", "Cannot follow a blocked user or you are blocked",
                    HttpStatus.BAD_REQUEST, null);
        }

        UserFollow follow = followRepository.findByFollowerUserIdAndFolloweeUserId(sourceId, targetId).orElse(null);
        if (follow != null && follow.getStatus() == RelationshipStatus.ACTIVE) {
            return APIResource.error("ALREADY_FOLLOWING", "Already following this user", HttpStatus.BAD_REQUEST, null);
        }

        if (follow == null) {
            follow = new UserFollow();
            follow.setFollowerUserId(sourceId);
            follow.setFolloweeUserId(targetId);
        }

        follow.setStatus(RelationshipStatus.ACTIVE);
        followRepository.save(follow);

        eventProducer.publishEvent(new RelationshipChangedEvent(sourceId, targetId,
                RelationshipChangedEvent.ChangeType.FOLLOW));

        return APIResource.ok("Followed successfully", toFollowResponse(follow));
    }

    @Override
    @Transactional
    public APIResource<Void> unfollow(UUID sourceId, UUID targetId) {
        UserFollow follow = followRepository.findByFollowerUserIdAndFolloweeUserId(sourceId, targetId).orElse(null);
        if (follow == null || follow.getStatus() == RelationshipStatus.REMOVED) {
            return APIResource.error("NOT_FOLLOWING", "Not following this user", HttpStatus.NOT_FOUND, null);
        }

        follow.setStatus(RelationshipStatus.REMOVED);
        followRepository.save(follow);

        eventProducer.publishEvent(new RelationshipChangedEvent(sourceId, targetId,
                RelationshipChangedEvent.ChangeType.UNFOLLOW));

        return APIResource.message("Unfollowed successfully", HttpStatus.OK);
    }

    @Override
    public APIResource<List<RelationshipResponse>> getFollowers(UUID userId) {
        List<RelationshipResponse> responses = followRepository.findAllByFolloweeUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(this::toFollowResponse)
                .collect(Collectors.toList());
        return APIResource.ok("Followers retrieved", responses);
    }

    @Override
    public APIResource<List<UUID>> getFollowersIds(UUID userId) {
        List<UUID> followers = followRepository.findAllByFolloweeUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(UserFollow::getFollowerUserId)
                .collect(Collectors.toList());
        return APIResource.ok("Follower IDs retrieved", followers);
    }

    @Override
    public APIResource<List<UUID>> getFollowingIds(UUID userId) {
        List<UUID> following = followRepository.findAllByFollowerUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(UserFollow::getFolloweeUserId)
                .collect(Collectors.toList());
        return APIResource.ok("Following IDs retrieved", following);
    }

    @Override
    public APIResource<Boolean> isFollowing(UUID sourceId, UUID targetId) {
        boolean exists = followRepository.existsByFollowerUserIdAndFolloweeUserIdAndStatus(
                sourceId, targetId, RelationshipStatus.ACTIVE);
        return APIResource.ok("Checked following status", exists);
    }

    @Override
    public APIResource<Boolean> isBlocked(UUID sourceId, UUID targetId) {
        boolean exists = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId);
        return APIResource.ok("Checked block status", exists);
    }

    @Override
    public APIResource<List<UUID>> getMutualFriends(UUID userId, UUID targetId) {
        Set<UUID> userFriends = new LinkedHashSet<>(getFriendIds(userId));
        List<UUID> mutual = getFriendIds(targetId).stream()
                .filter(userFriends::contains)
                .collect(Collectors.toList());
        return APIResource.ok("Mutual friends retrieved", mutual);
    }

    @Override
    public APIResource<List<RelationshipResponse>> searchFriends(UUID userId, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<RelationshipResponse> responses = friendRepository.findAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .filter(friend -> normalizedQuery.isBlank()
                        || getOtherFriendId(friend, userId).toString().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .map(friend -> toFriendResponse(friend, userId))
                .collect(Collectors.toList());
        return APIResource.ok("Search results retrieved", responses);
    }

    @Override
    public RelationshipStatusResponse getRelationshipStatus(UUID sourceId, UUID targetId) {
        boolean isFollowing = followRepository.existsByFollowerUserIdAndFolloweeUserIdAndStatus(
                sourceId, targetId, RelationshipStatus.ACTIVE);
        boolean isFollowedBy = followRepository.existsByFollowerUserIdAndFolloweeUserIdAndStatus(
                targetId, sourceId, RelationshipStatus.ACTIVE);
        boolean isFriend = findFriendship(sourceId, targetId)
                .map(friend -> friend.getStatus() == RelationshipStatus.ACTIVE)
                .orElse(false);
        boolean isBlocking = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId);
        boolean isBlockedBy = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(targetId, sourceId);

        FriendRequest friendRequest = friendRequestRepository
                .findBySenderUserIdAndReceiverUserIdAndStatus(sourceId, targetId, FriendRequestStatus.PENDING)
                .orElseGet(() -> friendRequestRepository
                        .findBySenderUserIdAndReceiverUserIdAndStatus(targetId, sourceId, FriendRequestStatus.PENDING)
                        .orElse(null));

        String friendRequestStatus = "NONE";
        UUID friendRequestId = null;
        if (friendRequest != null) {
            friendRequestId = friendRequest.getId();
            friendRequestStatus = friendRequest.getSenderUserId().equals(sourceId)
                    ? "PENDING_SENT"
                    : "PENDING_RECEIVED";
        }

        return RelationshipStatusResponse.builder()
                .isFollowing(isFollowing)
                .isFollowedBy(isFollowedBy)
                .isFriend(isFriend)
                .isBlocking(isBlocking)
                .isBlockedBy(isBlockedBy)
                .friendRequestStatus(friendRequestStatus)
                .friendRequestId(friendRequestId)
                .build();
    }

    @Override
    public APIResource<List<RelationshipResponse>> getFriendList(UUID userId) {
        List<RelationshipResponse> responses = friendRepository.findAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(friend -> toFriendResponse(friend, userId))
                .collect(Collectors.toList());
        return APIResource.ok("Friends retrieved", responses);
    }

    @Override
    public APIResource<PageResponse<RelationshipUserSummaryResponse>> getFriendList(UUID userId, int page, int size) {
        Pageable pageable = defaultPageable(page, size);
        Page<UserFriend> friends = friendRepository.findAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE, pageable);

        List<UUID> otherUserIds = friends.getContent().stream().map(friend -> getOtherFriendId(friend, userId)).toList();
        log.info("Fetching profiles for {} friends of user {}", otherUserIds.size(), userId);
        Map<UUID, UserProfileSummary> profiles = userProfileClient.getUserProfiles(otherUserIds);

        List<RelationshipUserSummaryResponse> content = friends.getContent().stream()
                .map(friend -> {
                    UUID otherUserId = getOtherFriendId(friend, userId);
                    return toUserSummary(friend.getId(), otherUserId, RelationshipType.FRIEND, friend.getAcceptedAt(), profiles);
                })
                .toList();

        return APIResource.ok("Friends retrieved", toPageResponse(friends, content));
    }

    @Override
    public APIResource<PageResponse<RelationshipUserSummaryResponse>> getFollowerList(UUID userId, int page, int size) {
        Pageable pageable = defaultPageable(page, size);
        Page<UserFollow> followers = followRepository.findAllByFolloweeUserIdAndStatus(
                userId, RelationshipStatus.ACTIVE, pageable);

        List<UUID> followerIds = followers.getContent().stream().map(UserFollow::getFollowerUserId).toList();
        log.info("Fetching profiles for {} followers of user {}", followerIds.size(), userId);
        Map<UUID, UserProfileSummary> profiles = userProfileClient.getUserProfiles(followerIds);

        List<RelationshipUserSummaryResponse> content = followers.getContent().stream()
                .map(follow -> {
                    UUID followerId = follow.getFollowerUserId();
                    return toUserSummary(follow.getId(), followerId, RelationshipType.FOLLOW, toLocalDateTime(follow.getCreatedAt()), profiles);
                })
                .toList();

        return APIResource.ok("Followers retrieved", toPageResponse(followers, content));
    }

    @Override
    public APIResource<Set<UUID>> getAudienceIds(UUID userId) {
        Set<UUID> audience = followRepository.findAllByFolloweeUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(UserFollow::getFollowerUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        audience.addAll(getFriendIds(userId));
        return APIResource.ok("Audience IDs retrieved", audience);
    }

    @Override
    @Transactional
    public APIResource<Void> unfriend(UUID sourceId, UUID targetId) {
        UserFriend friendship = findFriendship(sourceId, targetId).orElse(null);
        if (friendship == null || friendship.getStatus() == RelationshipStatus.REMOVED) {
            return APIResource.error("NOT_FRIENDS", "Users are not friends", HttpStatus.NOT_FOUND, null);
        }

        friendship.setStatus(RelationshipStatus.REMOVED);
        friendRepository.save(friendship);

        boolean removedSourceFollow = removeFollowIfActive(sourceId, targetId);
        boolean removedTargetFollow = removeFollowIfActive(targetId, sourceId);

        eventProducer.publishEvent(new RelationshipChangedEvent(sourceId, targetId,
                RelationshipChangedEvent.ChangeType.UNFRIEND));
        if (removedSourceFollow) {
            eventProducer.publishEvent(new RelationshipChangedEvent(sourceId, targetId,
                    RelationshipChangedEvent.ChangeType.UNFOLLOW));
        }
        if (removedTargetFollow) {
            eventProducer.publishEvent(new RelationshipChangedEvent(targetId, sourceId,
                    RelationshipChangedEvent.ChangeType.UNFOLLOW));
        }

        return APIResource.message("Unfriended successfully", HttpStatus.OK);
    }

    private boolean isBlockedEitherDirection(UUID sourceId, UUID targetId) {
        return userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId)
                || userBlockRepository.existsByBlockerUserIdAndBlockedUserId(targetId, sourceId);
    }

    private List<UUID> getFriendIds(UUID userId) {
        return friendRepository.findAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(friend -> getOtherFriendId(friend, userId))
                .collect(Collectors.toList());
    }

    private java.util.Optional<UserFriend> findFriendship(UUID sourceId, UUID targetId) {
        UUID first = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
        UUID second = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;
        return friendRepository.findByUser1IdAndUser2Id(first, second);
    }

    private UUID getOtherFriendId(UserFriend friend, UUID userId) {
        return friend.getUser1Id().equals(userId) ? friend.getUser2Id() : friend.getUser1Id();
    }

    private boolean removeFollowIfActive(UUID followerId, UUID followeeId) {
        UserFollow follow = followRepository.findByFollowerUserIdAndFolloweeUserId(followerId, followeeId).orElse(null);
        if (follow == null || follow.getStatus() == RelationshipStatus.REMOVED) {
            return false;
        }

        follow.setStatus(RelationshipStatus.REMOVED);
        followRepository.save(follow);
        return true;
    }

    private RelationshipResponse toFollowResponse(UserFollow follow) {
        return RelationshipResponse.builder()
                .id(follow.getId())
                .sourceUserId(follow.getFollowerUserId())
                .targetUserId(follow.getFolloweeUserId())
                .relationshipType(RelationshipType.FOLLOW)
                .status(follow.getStatus())
                .isCloseFriend(false)
                .isFavorite(false)
                .sourceNickname(follow.getSourceNickname())
                .createdAt(toLocalDateTime(follow.getCreatedAt()))
                .build();
    }

    private RelationshipResponse toFriendResponse(UserFriend friend, UUID perspectiveUserId) {
        return RelationshipResponse.builder()
                .id(friend.getId())
                .sourceUserId(perspectiveUserId)
                .targetUserId(getOtherFriendId(friend, perspectiveUserId))
                .relationshipType(RelationshipType.FRIEND)
                .status(friend.getStatus())
                .isCloseFriend(false)
                .isFavorite(false)
                .sourceNickname(null)
                .createdAt(toLocalDateTime(friend.getCreatedAt()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Pageable defaultPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, 100) : 20;
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String generateFallbackAvatar(String name) {
        String initial = "U";
        if (org.springframework.util.StringUtils.hasText(name)) {
            initial = name.substring(0, 1).toUpperCase();
        }
        return String.format("https://ui-avatars.com/api/?name=%s&background=random&color=fff", initial);
    }

    private RelationshipUserSummaryResponse toUserSummary(
            UUID relationshipId, 
            UUID userId, 
            RelationshipType type, 
            LocalDateTime createdAt, 
            Map<UUID, UserProfileSummary> profiles) {
        
        UserProfileSummary profile = profiles.get(userId);
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

        return RelationshipUserSummaryResponse.builder()
                .relationshipId(relationshipId)
                .userId(userId)
                .username(username)
                .displayName(displayName)
                .profilePictureUrl(profilePictureUrl)
                .relationshipType(type)
                .createdAt(createdAt)
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<?> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
