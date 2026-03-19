package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getByUsername(String username);

    @EntityGraph(attributePaths = {
            "profile",
            "profile.pictures",
            "emails",
            "phones"
    })
    @Transactional(readOnly = true)
    Optional<User> findDetailById(UUID userId);

    @Transactional()
    Boolean existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse (String username);
}
