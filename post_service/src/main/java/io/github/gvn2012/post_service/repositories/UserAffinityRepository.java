package io.github.gvn2012.post_service.repositories;

import io.github.gvn2012.post_service.entities.UserAffinity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAffinityRepository extends JpaRepository<UserAffinity, UUID> {

    @Query("SELECT u FROM UserAffinity u WHERE u.userId = :userId ORDER BY u.affinityScore DESC")
    List<UserAffinity> findTopAffinities(@Param("userId") UUID userId, Pageable pageable);

    Optional<UserAffinity> findByUserIdAndAuthorId(UUID userId, UUID authorId);

    List<UserAffinity> findByUserIdInAndAuthorId(List<UUID> userIds, UUID authorId);

    List<UserAffinity> findByUserIdAndAuthorIdIn(UUID userId, List<UUID> authorIds);
}
