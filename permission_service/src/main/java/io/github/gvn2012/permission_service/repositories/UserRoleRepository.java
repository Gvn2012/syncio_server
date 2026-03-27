package io.github.gvn2012.permission_service.repositories;

import io.github.gvn2012.permission_service.entities.UserRole;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    @EntityGraph(attributePaths = {
           "role"
    })
    Optional<UserRole> findByUserId(@NotNull UUID userId);
}
