package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserRelationshipStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRelationshipStatsRepository extends JpaRepository<UserRelationshipStats, UUID> {
    Optional<UserRelationshipStats> findByUserId(UUID userId);
}
