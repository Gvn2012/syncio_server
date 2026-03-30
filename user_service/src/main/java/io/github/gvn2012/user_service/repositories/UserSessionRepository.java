package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUser_IdAndRevokedFalse(UUID userId);

    Optional<UserSession> findByIdAndUser_Id(UUID id, UUID userId);

    List<UserSession> findByUser_IdAndRevokedFalseAndExpiresAtAfter(UUID userId, Instant now);

    long countByUser_IdAndRevokedFalse(UUID userId);
}
