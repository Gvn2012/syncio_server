package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserEmailRepository extends JpaRepository <UserEmail, UUID>{
    Optional<UserEmail> findByIdAndUser_IdAndStatus(UUID id, UUID userId, EmailStatus status);

    Optional<UserEmail> findByIdAndUser_IdAndStatusIn(UUID id, UUID userId, List<EmailStatus> statuses);

    boolean existsByEmailAndStatusNot(String email, EmailStatus status);

    Set<UserEmail> findAllByUser_Id(UUID userId);

    Optional<UserEmail> findByUser_IdAndPrimaryTrueAndStatusNot(UUID userId, EmailStatus excludedStatus);

    long countByUser_IdAndStatusNot(UUID userId, EmailStatus excludedStatus);
}
