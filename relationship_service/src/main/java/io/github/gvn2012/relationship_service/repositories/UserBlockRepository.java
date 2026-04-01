package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);
    long countAllByBlockerId(UUID blockerId);
    long countAllByBlockedId(UUID blockedId);
}
