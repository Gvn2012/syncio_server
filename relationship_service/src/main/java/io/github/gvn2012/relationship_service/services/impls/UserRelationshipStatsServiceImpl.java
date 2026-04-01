package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.UserRelationshipStats;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
import io.github.gvn2012.relationship_service.repositories.MutualFriendshipRepository;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserMuteRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipRepository;
import io.github.gvn2012.relationship_service.repositories.UserRelationshipStatsRepository;
import io.github.gvn2012.relationship_service.services.interfaces.IUserRelationshipStatsService;
import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRelationshipStatsServiceImpl implements IUserRelationshipStatsService {

    private final UserRelationshipStatsRepository statsRepository;
    private final UserRelationshipRepository relationshipRepository;
    private final UserBlockRepository blockRepository;
    private final UserMuteRepository muteRepository;
    private final MutualFriendshipRepository mutualFriendshipRepository;

    @Override
    @KafkaListener(topics = "relationship-events", groupId = "relationship-stats-group")
    @Transactional
    public void handleRelationshipChange(RelationshipChangedEvent event) {
        log.info("Handling relationship change for stats: {} between {} and {}", 
            event.getChangeType(), event.getSourceUserId(), event.getTargetUserId());
        
        updateStatsForUser(event.getSourceUserId());
        updateStatsForUser(event.getTargetUserId());

        if (event.getChangeType() == RelationshipChangedEvent.ChangeType.FRIEND_REQUEST_ACCEPTED) {
            updateMutualFriendships(event.getSourceUserId(), event.getTargetUserId());
        }
    }

    private void updateMutualFriendships(UUID userA, UUID userB) {
        // Find all friends of UserA
        List<UUID> friendsA = relationshipRepository.findAllBySourceUserIdAndStatus(userA, RelationshipStatus.ACTIVE)
                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                .map(UserRelationship::getTargetUserId).toList();

        // For each friend F of A, F and B now have one more mutual friend (A)
        for (UUID friendId : friendsA) {
            if (!friendId.equals(userB)) {
                incrementMutualCount(friendId, userB);
            }
        }

        // Find all friends of UserB
        List<UUID> friendsB = relationshipRepository.findAllBySourceUserIdAndStatus(userB, RelationshipStatus.ACTIVE)
                .stream().filter(r -> r.getRelationshipType() == RelationshipType.FRIEND)
                .map(UserRelationship::getTargetUserId).toList();

        // For each friend F of B, F and A now have one more mutual friend (B)
        for (UUID friendId : friendsB) {
            if (!friendId.equals(userA)) {
                incrementMutualCount(friendId, userA);
            }
        }
    }

    private void incrementMutualCount(UUID u1, UUID u2) {
        UUID first = u1.compareTo(u2) < 0 ? u1 : u2;
        UUID second = u1.compareTo(u2) < 0 ? u2 : u1;

        io.github.gvn2012.relationship_service.entities.MutualFriendship mutual = 
            mutualFriendshipRepository.findByUser1IdAndUser2Id(first, second)
                .orElseGet(() -> {
                    return io.github.gvn2012.relationship_service.entities.MutualFriendship.builder()
                        .user1Id(first)
                        .user2Id(second)
                        .mutualCount(0)
                        .build();
                });

        mutual.setMutualCount(mutual.getMutualCount() + 1);
        mutualFriendshipRepository.save(mutual);
    }

    @Override
    public UserRelationshipStats getStatsByUserId(UUID userId) {
        return statsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStats(userId));
    }

    @Override
    @Transactional
    public void recalculateStats(UUID userId) {
        updateStatsForUser(userId);
    }

    private void updateStatsForUser(UUID userId) {
        UserRelationshipStats stats = getStatsByUserId(userId);
        
        // Count followers (target_user_id = userId, type = FOLLOW)
        stats.setFollowerCount((long) countActive(userId, RelationshipType.FOLLOW, false));
        // Count following (source_user_id = userId, type = FOLLOW)
        stats.setFollowingCount((long) countActive(userId, RelationshipType.FOLLOW, true));
        // Count friends
        stats.setFriendCount((long) countActive(userId, RelationshipType.FRIEND, true));
        
        // Count blocks/mutes
        stats.setBlockedCount((long) blockRepository.countAllByBlockerId(userId));
        stats.setBlockedByCount((long) blockRepository.countAllByBlockedId(userId));
        stats.setMutedCount((long) muteRepository.countAllByMuterUserIdAndIsActive(userId, true));

        stats.setLastCalculatedAt(LocalDateTime.now());
        statsRepository.save(stats);
    }

    private int countActive(UUID userId, RelationshipType type, boolean isSource) {
        if (isSource) {
            return relationshipRepository.findAllBySourceUserIdAndStatus(userId, RelationshipStatus.ACTIVE).stream()
                    .filter(r -> r.getRelationshipType() == type)
                    .toList().size();
        } else {
            return relationshipRepository.findAllByTargetUserIdAndStatus(userId, RelationshipStatus.ACTIVE).stream()
                    .filter(r -> r.getRelationshipType() == type)
                    .toList().size();
        }
    }

    private UserRelationshipStats createDefaultStats(UUID userId) {
        UserRelationshipStats stats = new UserRelationshipStats();
        stats.setUserId(userId);
        return statsRepository.save(stats);
    }
}
