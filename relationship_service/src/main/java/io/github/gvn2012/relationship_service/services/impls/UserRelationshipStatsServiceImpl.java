package io.github.gvn2012.relationship_service.services.impls;

import io.github.gvn2012.relationship_service.entities.MutualFriendship;
import io.github.gvn2012.relationship_service.entities.ProcessedEvent;
import io.github.gvn2012.relationship_service.entities.UserRelationshipStats;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.repositories.MutualFriendshipRepository;
import io.github.gvn2012.relationship_service.repositories.ProcessedEventRepository;
import io.github.gvn2012.relationship_service.repositories.UserBlockRepository;
import io.github.gvn2012.relationship_service.repositories.UserFollowRepository;
import io.github.gvn2012.relationship_service.repositories.UserFriendRepository;
import io.github.gvn2012.relationship_service.repositories.UserMuteRepository;
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
    private final UserFollowRepository followRepository;
    private final UserFriendRepository friendRepository;
    private final UserBlockRepository blockRepository;
    private final UserMuteRepository muteRepository;
    private final MutualFriendshipRepository mutualFriendshipRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Override
    @KafkaListener(topics = "relationship-events-v2", groupId = "relationship-stats-group")
    @Transactional
    public void handleRelationshipChange(RelationshipChangedEvent event) {
        if (event.getEventId() != null) {
            String eventIdStr = event.getEventId().toString();
            if (processedEventRepository.existsById(eventIdStr)) {
                log.info("Duplicate relationship event dropped: {}", eventIdStr);
                return;
            }
            processedEventRepository.save(new ProcessedEvent(eventIdStr, LocalDateTime.now()));
        }

        log.info("Handling relationship change for stats: {} between {} and {}",
                event.getChangeType(), event.getSourceUserId(), event.getTargetUserId());

        updateStatsForUser(event.getSourceUserId());
        updateStatsForUser(event.getTargetUserId());

        if (event.getChangeType() == RelationshipChangedEvent.ChangeType.FRIEND_REQUEST_ACCEPTED) {
            updateMutualFriendships(event.getSourceUserId(), event.getTargetUserId());
        }
    }

    @Override
    public UserRelationshipStats getStatsByUserId(UUID userId) {
        return statsRepository.findByUserId(userId).orElseGet(() -> createDefaultStats(userId));
    }

    @Override
    @Transactional
    public void recalculateStats(UUID userId) {
        updateStatsForUser(userId);
    }

    private void updateStatsForUser(UUID userId) {
        UserRelationshipStats stats = getStatsByUserId(userId);

        stats.setFollowerCount(followRepository.countByFolloweeUserIdAndStatus(userId, RelationshipStatus.ACTIVE));
        stats.setFollowingCount(followRepository.countByFollowerUserIdAndStatus(userId, RelationshipStatus.ACTIVE));
        stats.setFriendCount(friendRepository.countAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE));
        stats.setBlockedCount((long) blockRepository.countAllByBlockerUserId(userId));
        stats.setBlockedByCount((long) blockRepository.countAllByBlockedUserId(userId));
        stats.setMutedCount((long) muteRepository.countAllByMuterUserIdAndIsActive(userId, true));
        stats.setLastCalculatedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    private void updateMutualFriendships(UUID userA, UUID userB) {
        List<UUID> friendsA = getFriendIds(userA);
        for (UUID friendId : friendsA) {
            if (!friendId.equals(userB)) {
                incrementMutualCount(friendId, userB);
            }
        }

        List<UUID> friendsB = getFriendIds(userB);
        for (UUID friendId : friendsB) {
            if (!friendId.equals(userA)) {
                incrementMutualCount(friendId, userA);
            }
        }
    }

    private List<UUID> getFriendIds(UUID userId) {
        return friendRepository.findAllByUserIdAndStatus(userId, RelationshipStatus.ACTIVE)
                .stream()
                .map(friend -> friend.getUser1Id().equals(userId) ? friend.getUser2Id() : friend.getUser1Id())
                .toList();
    }

    private void incrementMutualCount(UUID userA, UUID userB) {
        UUID first = userA.compareTo(userB) < 0 ? userA : userB;
        UUID second = userA.compareTo(userB) < 0 ? userB : userA;

        MutualFriendship mutual = mutualFriendshipRepository.findByUser1IdAndUser2Id(first, second)
                .orElseGet(() -> MutualFriendship.builder()
                        .user1Id(first)
                        .user2Id(second)
                        .mutualCount(0)
                        .build());

        mutual.setMutualCount(mutual.getMutualCount() + 1);
        mutualFriendshipRepository.save(mutual);
    }

    private UserRelationshipStats createDefaultStats(UUID userId) {
        UserRelationshipStats stats = new UserRelationshipStats();
        stats.setUserId(userId);
        return statsRepository.save(stats);
    }
}
