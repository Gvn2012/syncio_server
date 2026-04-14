package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Optional<UserBlock> findByBlockerUserIdAndBlockedUserId(UUID blockerId, UUID blockedId);

    boolean existsByBlockerUserIdAndBlockedUserId(UUID blockerId, UUID blockedId);

    long countAllByBlockerUserId(UUID blockerId);

    long countAllByBlockedUserId(UUID blockedId);

    boolean existsByBlockerUserIdAndBlockedUserIdAndIsActiveTrue(UUID userIdF, UUID userIdS);

    java.util.List<io.github.gvn2012.relationship_service.entities.UserBlock> findAllByBlockerUserIdAndIsActiveTrue(UUID blockerId);

    java.util.List<io.github.gvn2012.relationship_service.entities.UserBlock> findAllByBlockedUserIdAndIsActiveTrue(UUID blockedId);
 
    org.springframework.data.domain.Page<io.github.gvn2012.relationship_service.entities.UserBlock> findAllByBlockerUserIdAndIsActiveTrue(UUID blockerId, org.springframework.data.domain.Pageable pageable);
}
