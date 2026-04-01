package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserRelationship;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, UUID> {

    Optional<UserRelationship> findBySourceUserIdAndTargetUserIdAndRelationshipType(
            UUID sourceUserId, UUID targetUserId, RelationshipType relationshipType);

    List<UserRelationship> findAllBySourceUserIdAndStatus(UUID sourceUserId, RelationshipStatus status);

    List<UserRelationship> findAllByTargetUserIdAndStatus(UUID targetUserId, RelationshipStatus status);

    boolean existsBySourceUserIdAndTargetUserIdAndRelationshipTypeAndStatus(
            UUID sourceUserId, UUID targetUserId, RelationshipType relationshipType, RelationshipStatus status);
}
