package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.mappers.RelationshipMapper;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipResponse;
import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IRelationshipService;
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
public class RelationshipServiceImpl implements IRelationshipService {

        private final UserRelationshipRepository relationshipRepository;
        private final UserBlockRepository userBlockRepository;
        private final io.github.gvn2012.relationship_service.repositories.FriendRequestRepository friendRequestRepository;
        private final RelationshipMapper relationshipMapper;
        private final RelationshipEventProducer eventProducer;

        @Override
        @Transactional
        public APIResource<RelationshipResponse> follow(UUID sourceId, UUID targetId) {
                if (sourceId.equals(targetId)) {
                        return APIResource.error("SELF_FOLLOW", "Cannot follow yourself", HttpStatus.BAD_REQUEST, null);
                }

                UserRelationship relationship = relationshipRepository
                                .findBySourceUserIdAndTargetUserIdAndRelationshipType(
                                                sourceId, targetId, RelationshipType.FOLLOW)
                                .orElse(null);

                if (relationship != null && relationship.getStatus() == RelationshipStatus.ACTIVE) {
                        return APIResource.error("ALREADY_FOLLOWING", "Already following this user",
                                        HttpStatus.BAD_REQUEST, null);
                }

                if (userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId) ||
                                userBlockRepository.existsByBlockerUserIdAndBlockedUserId(targetId, sourceId)) {
                        return APIResource.error("BLOCKED", "Cannot follow a blocked user or you are blocked",
                                        HttpStatus.BAD_REQUEST, null);
                }

                if (relationship == null) {
                        relationship = new UserRelationship();
                        relationship.setSourceUserId(sourceId);
                        relationship.setTargetUserId(targetId);
                        relationship.setRelationshipType(RelationshipType.FOLLOW);
                }

                relationship.setStatus(RelationshipStatus.ACTIVE);
                relationshipRepository.save(relationship);

                eventProducer.publishEvent(
                                new RelationshipChangedEvent(sourceId, targetId,
                                                RelationshipChangedEvent.ChangeType.FOLLOW));

                return APIResource.ok("Followed successfully", relationshipMapper.toResponse(relationship));
        }

        @Override
        @Transactional
        public APIResource<Void> unfollow(UUID sourceId, UUID targetId) {
                UserRelationship relationship = relationshipRepository
                                .findBySourceUserIdAndTargetUserIdAndRelationshipType(
                                                sourceId, targetId, RelationshipType.FOLLOW)
                                .orElse(null);

                if (relationship == null || relationship.getStatus() == RelationshipStatus.REMOVED) {
                        return APIResource.error("NOT_FOLLOWING", "Not following this user", HttpStatus.NOT_FOUND,
                                        null);
                }

                relationship.setStatus(RelationshipStatus.REMOVED);
                relationshipRepository.save(relationship);

                eventProducer.publishEvent(
                                new RelationshipChangedEvent(sourceId, targetId,
                                                RelationshipChangedEvent.ChangeType.UNFOLLOW));

                return APIResource.message("Unfollowed successfully", HttpStatus.OK);
        }

        @Override
        public APIResource<List<RelationshipResponse>> getFollowers(UUID userId) {
                List<UserRelationship> followers = relationshipRepository.findAllByTargetUserIdAndStatus(userId,
                                RelationshipStatus.ACTIVE);
                List<RelationshipResponse> responses = followers.stream()
                                .map(relationshipMapper::toResponse)
                                .collect(Collectors.toList());
                return APIResource.ok("Followers retrieved", responses);
        }

        @Override
        public APIResource<List<UUID>> getFollowersIds(UUID userId) {
                List<UUID> followers = relationshipRepository
                                .findAllByTargetUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream().map(UserRelationship::getSourceUserId).collect(Collectors.toList());
                return APIResource.ok("Follower IDs retrieved", followers);
        }

        @Override
        public APIResource<List<UUID>> getFollowingIds(UUID userId) {
                List<UUID> following = relationshipRepository
                                .findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FOLLOW)
                                .map(UserRelationship::getTargetUserId).collect(Collectors.toList());
                return APIResource.ok("Following IDs retrieved", following);
        }

        @Override
        public APIResource<Boolean> isFollowing(UUID sourceId, UUID targetId) {
                boolean exists = relationshipRepository.existsBySourceUserIdAndTargetUserIdAndRelationshipTypeAndStatus(
                                sourceId, targetId, RelationshipType.FOLLOW, RelationshipStatus.ACTIVE);
                return APIResource.ok("Checked following status", exists);
        }

        @Override
        public APIResource<Boolean> isBlocked(UUID sourceId, UUID targetId) {
                boolean exists = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId);
                return APIResource.ok("Checked block status", exists);
        }

        @Override
        public APIResource<List<UUID>> getMutualFriends(UUID userId, UUID targetId) {
                List<UUID> userFriends = relationshipRepository
                                .findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                                .map(UserRelationship::getTargetUserId).toList();

                List<UUID> targetFriends = relationshipRepository
                                .findAllBySourceUserIdAndStatus(targetId, RelationshipStatus.ACTIVE)
                                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                                .map(UserRelationship::getTargetUserId).toList();

                List<UUID> mutual = userFriends.stream()
                                .filter(targetFriends::contains)
                                .collect(Collectors.toList());

                return APIResource.ok("Mutual friends retrieved", mutual);
        }

        @Override
        public APIResource<List<RelationshipResponse>> searchFriends(UUID userId, String query) {
                List<UserRelationship> friends = relationshipRepository
                                .findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                                .filter(r -> r.getSourceNickname() != null
                                                && r.getSourceNickname().toLowerCase().contains(query.toLowerCase()))
                                .collect(Collectors.toList());

                List<RelationshipResponse> responses = friends.stream()
                                .map(relationshipMapper::toResponse)
                                .collect(Collectors.toList());

                return APIResource.ok("Search results retrieved", responses);
        }

        @Override
        public io.github.gvn2012.relationship_service.dtos.responses.RelationshipStatusResponse getRelationshipStatus(
                        UUID sourceId, UUID targetId) {
                boolean isFollowing = relationshipRepository
                                .existsBySourceUserIdAndTargetUserIdAndRelationshipTypeAndStatus(
                                                sourceId, targetId, RelationshipType.FOLLOW, RelationshipStatus.ACTIVE);
                boolean isFollowedBy = relationshipRepository
                                .existsBySourceUserIdAndTargetUserIdAndRelationshipTypeAndStatus(
                                                targetId, sourceId, RelationshipType.FOLLOW, RelationshipStatus.ACTIVE);
                boolean isFriend = relationshipRepository
                                .existsBySourceUserIdAndTargetUserIdAndRelationshipTypeAndStatus(
                                                sourceId, targetId, RelationshipType.FRIEND, RelationshipStatus.ACTIVE);
                boolean isBlocking = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(sourceId, targetId);
                boolean isBlockedBy = userBlockRepository.existsByBlockerUserIdAndBlockedUserId(targetId, sourceId);

                String frStatus = "NONE";
                if (friendRequestRepository.existsBySenderUserIdAndReceiverUserIdAndStatus(sourceId, targetId,
                                io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus.PENDING)) {
                        frStatus = "PENDING_SENT";
                } else if (friendRequestRepository.existsBySenderUserIdAndReceiverUserIdAndStatus(targetId, sourceId,
                                io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus.PENDING)) {
                        frStatus = "PENDING_RECEIVED";
                }

                return io.github.gvn2012.relationship_service.dtos.responses.RelationshipStatusResponse.builder()
                                .isFollowing(isFollowing)
                                .isFollowedBy(isFollowedBy)
                                .isFriend(isFriend)
                                .isBlocking(isBlocking)
                                .isBlockedBy(isBlockedBy)
                                .friendRequestStatus(frStatus)
                                .build();
        }

        @Override
        public APIResource<List<RelationshipResponse>> getFriendList(UUID userId) {
                List<UserRelationship> friends = relationshipRepository
                                .findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                                .collect(Collectors.toList());
                List<RelationshipResponse> responses = friends.stream()
                                .map(relationshipMapper::toResponse)
                                .collect(Collectors.toList());
                return APIResource.ok("Friends retrieved", responses);
        }

        @Override
        public APIResource<java.util.Set<UUID>> getAudienceIds(UUID userId) {
                List<UserRelationship> allRelations = relationshipRepository.findAllByTargetUserIdAndStatus(userId,
                                RelationshipStatus.ACTIVE);
                java.util.Set<UUID> audience = allRelations.stream()
                                .map(UserRelationship::getSourceUserId)
                                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

                relationshipRepository.findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                                .stream()
                                .filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                                .map(UserRelationship::getTargetUserId)
                                .forEach(audience::add);

                return APIResource.ok("Audience IDs retrieved", audience);
        }
}
