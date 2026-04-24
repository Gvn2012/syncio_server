package io.github.gvn2012.post_service.services.impls;

import io.github.gvn2012.post_service.clients.RelationshipClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialRelationshipService {

    private final RelationshipClient relationshipClient;

    @Cacheable(value = "userFriends", key = "#userId")
    public Set<UUID> getFriendIds(UUID userId) {
        return relationshipClient.getFriendIds(userId).block();
    }

    @Cacheable(value = "userBlocks", key = "#userId")
    public List<UUID> getBlockedList(UUID userId) {
        return relationshipClient.getBlockedList(userId).block();
    }

    @Cacheable(value = "userBlockedBy", key = "#userId")
    public List<UUID> getBlockedByList(UUID userId) {
        return relationshipClient.getBlockedByList(userId).block();
    }
}
