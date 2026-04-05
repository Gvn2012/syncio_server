package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.PendingEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PendingEmailVerificationRepository extends JpaRepository<PendingEmailVerification, UUID> {

    Optional<PendingEmailVerification> findByIdAndConsumedAtIsNull(UUID id);
}
