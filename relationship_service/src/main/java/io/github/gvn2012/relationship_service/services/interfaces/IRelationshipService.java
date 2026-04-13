package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.relationship_service.dtos.APIResource;
import io.github.gvn2012.relationship_service.dtos.responses.RelationshipResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IRelationshipService {
    APIResource<RelationshipResponse> follow(UUID sourceId, UUID targetId);

    APIResource<Void> unfollow(UUID sourceId, UUID targetId);

    APIResource<List<RelationshipResponse>> getFollowers(UUID userId);

    APIResource<List<UUID>> getFollowersIds(UUID userId);

    APIResource<List<UUID>> getFollowingIds(UUID userId);

    APIResource<Boolean> isFollowing(UUID sourceId, UUID targetId);

    APIResource<Boolean> isBlocked(UUID sourceId, UUID targetId);

    APIResource<List<UUID>> getMutualFriends(UUID userId, UUID targetId);

    APIResource<List<RelationshipResponse>> searchFriends(UUID userId, String query);

    io.github.gvn2012.relationship_service.dtos.responses.RelationshipStatusResponse getRelationshipStatus(
            UUID sourceId, UUID targetId);

    APIResource<List<RelationshipResponse>> getFriendList(UUID userId);

    APIResource<Void> unfriend(UUID sourceId, UUID targetId);

    APIResource<Set<UUID>> getAudienceIds(UUID userId);
}
