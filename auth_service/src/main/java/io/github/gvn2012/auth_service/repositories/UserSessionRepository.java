package io.github.gvn2012.auth_service.repositories;

import io.github.gvn2012.auth_service.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findBySessionTokenHashAndRevokedFalse(String sessionTokenHash);
    List<UserSession> findByUserIdAndRevokedFalse(UUID userId);
}
