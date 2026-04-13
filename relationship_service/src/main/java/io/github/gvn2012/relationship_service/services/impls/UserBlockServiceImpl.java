package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.CheckBlockStatusResponse;
import io.github.gvn2012.relationship_service.entities.UserBlock;
import io.github.gvn2012.relationship_service.entities.UserFollow;
import io.github.gvn2012.relationship_service.entities.UserFriend;
import io.github.gvn2012.relationship_service.entities.enums.BlockReason;
import io.github.gvn2012.relationship_service.entities.enums.BlockScope;
import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.repositories.FriendRequestRepository;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserFollowRepository;
import io.github.gvn2012.relationship_service.repositories.UserFriendRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IUserBlockService;
import io.github.gvn2012.relationship_service.services.kafka.RelationshipEventProducer;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements IUserBlockService {

    private final UserBlockRepository blockRepository;
    private final UserFollowRepository followRepository;
    private final UserFriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final RelationshipEventProducer eventProducer;

    @Override
    @Transactional
    public APIResource<Void> blockUser(UUID blockerId, UUID blockedId, BlockReason reason, String notes) {
        if (blockerId.equals(blockedId)) {
            return APIResource.error("SELF_BLOCK", "Cannot block yourself", HttpStatus.BAD_REQUEST, null);
        }

        if (blockRepository.existsByBlockerUserIdAndBlockedUserId(blockerId, blockedId)) {
            return APIResource.error("ALREADY_BLOCKED", "User is already blocked", HttpStatus.BAD_REQUEST, null);
        }

        UserBlock block = new UserBlock();
        block.setBlockerUserId(blockerId);
        block.setBlockedUserId(blockedId);
        block.setReason(reason);
        block.setReasonNote(notes);
        block.setScope(BlockScope.FULL);
        blockRepository.save(block);

        severFollow(blockerId, blockedId);
        severFollow(blockedId, blockerId);
        severFriendship(blockerId, blockedId);
        cancelPendingFriendRequests(blockerId, blockedId);

        eventProducer.publishEvent(new RelationshipChangedEvent(blockerId, blockedId,
                RelationshipChangedEvent.ChangeType.BLOCK));

        return APIResource.message("User blocked successfully", HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<Void> unblockUser(UUID blockerId, UUID blockedId) {
        UserBlock block = blockRepository.findByBlockerUserIdAndBlockedUserId(blockerId, blockedId).orElse(null);
        if (block == null) {
            return APIResource.error("NOT_BLOCKED", "User is not blocked", HttpStatus.NOT_FOUND, null);
        }

        blockRepository.delete(block);
        eventProducer.publishEvent(new RelationshipChangedEvent(blockerId, blockedId,
                RelationshipChangedEvent.ChangeType.UNBLOCK));

        return APIResource.message("User unblocked successfully", HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckBlockStatusResponse checkBlockStatus(UUID userIdF, UUID userIdS) {
        boolean fDirection = blockRepository.existsByBlockerUserIdAndBlockedUserIdAndIsActiveTrue(userIdF, userIdS);
        boolean sDirection = blockRepository.existsByBlockerUserIdAndBlockedUserIdAndIsActiveTrue(userIdS, userIdF);

        return CheckBlockStatusResponse.builder()
                .isBlocked(fDirection || sDirection)
                .isBidirectionalBlocked(fDirection && sDirection)
                .blockerId((fDirection && sDirection) ? null : (fDirection ? userIdF : (sDirection ? userIdS : null)))
                .build();
    }

    @Override
    public List<UUID> getBlockedList(UUID userId) {
        return blockRepository.findAllByBlockerUserIdAndIsActiveTrue(userId)
                .stream()
                .map(UserBlock::getBlockedUserId)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getBlockedByList(UUID userId) {
        return blockRepository.findAllByBlockedUserIdAndIsActiveTrue(userId)
                .stream()
                .map(UserBlock::getBlockerUserId)
                .collect(Collectors.toList());
    }

    private void severFollow(UUID followerId, UUID followeeId) {
        UserFollow follow = followRepository.findByFollowerUserIdAndFolloweeUserId(followerId, followeeId).orElse(null);
        if (follow == null) {
            return;
        }

        follow.setStatus(RelationshipStatus.REMOVED);
        followRepository.save(follow);
    }

    private void severFriendship(UUID userA, UUID userB) {
        UUID first = userA.compareTo(userB) < 0 ? userA : userB;
        UUID second = userA.compareTo(userB) < 0 ? userB : userA;
        UserFriend friend = friendRepository.findByUser1IdAndUser2Id(first, second).orElse(null);
        if (friend == null) {
            return;
        }

        friend.setStatus(RelationshipStatus.REMOVED);
        friendRepository.save(friend);
    }

    private void cancelPendingFriendRequests(UUID userA, UUID userB) {
        friendRequestRepository.findBySenderUserIdAndReceiverUserIdAndStatus(userA, userB, FriendRequestStatus.PENDING)
                .ifPresent(req -> {
                    req.setStatus(FriendRequestStatus.CANCELLED);
                    friendRequestRepository.save(req);
                });
        friendRequestRepository.findBySenderUserIdAndReceiverUserIdAndStatus(userB, userA, FriendRequestStatus.PENDING)
                .ifPresent(req -> {
                    req.setStatus(FriendRequestStatus.CANCELLED);
                    friendRequestRepository.save(req);
                });
    }
}
