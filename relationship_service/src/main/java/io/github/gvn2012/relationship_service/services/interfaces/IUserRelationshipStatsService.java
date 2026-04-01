package io.github.gvn2012.relationship_service.services.interfaces;

import io.github.gvn2012.shared.kafka_events.RelationshipChangedEvent;
import io.github.gvn2012.relationship_service.entities.UserRelationshipStats;

import java.util.UUID;

public interface IUserRelationshipStatsService {
    void handleRelationshipChange(RelationshipChangedEvent event);
    UserRelationshipStats getStatsByUserId(UUID userId);
    void recalculateStats(UUID userId);
}
