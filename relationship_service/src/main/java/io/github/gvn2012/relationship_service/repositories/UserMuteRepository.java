package io.github.gvn2012.relationship_service.repositories;

import io.github.gvn2012.relationship_service.entities.UserMute;
import io.github.gvn2012.relationship_service.entities.enums.MuteScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMuteRepository extends JpaRepository<UserMute, UUID> {
    Optional<UserMute> findByMuterUserIdAndMutedUserIdAndScope(UUID muterId, UUID mutedId, MuteScope scope);
    List<UserMute> findAllByMuterUserIdAndMutedUserId(UUID muterId, UUID mutedId);
    long countAllByMuterUserIdAndIsActive(UUID muterId, boolean isActive);
}
