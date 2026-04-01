package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.MutualFriendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MutualFriendshipRepository extends JpaRepository<MutualFriendship, UUID> {
    Optional<MutualFriendship> findByUser1IdAndUser2Id(UUID user1Id, UUID user2Id);
}
