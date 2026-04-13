package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserFollow;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByFollowerUserIdAndFolloweeUserId(UUID followerUserId, UUID followeeUserId);

    boolean existsByFollowerUserIdAndFolloweeUserIdAndStatus(
            UUID followerUserId, UUID followeeUserId, RelationshipStatus status);

    List<UserFollow> findAllByFollowerUserIdAndStatus(UUID followerUserId, RelationshipStatus status);

    List<UserFollow> findAllByFolloweeUserIdAndStatus(UUID followeeUserId, RelationshipStatus status);

    Page<UserFollow> findAllByFollowerUserIdAndStatus(UUID followerUserId, RelationshipStatus status, Pageable pageable);

    Page<UserFollow> findAllByFolloweeUserIdAndStatus(UUID followeeUserId, RelationshipStatus status, Pageable pageable);

    long countByFollowerUserIdAndStatus(UUID followerUserId, RelationshipStatus status);

    long countByFolloweeUserIdAndStatus(UUID followeeUserId, RelationshipStatus status);
}
