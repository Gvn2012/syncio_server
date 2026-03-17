package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getByUsername(String username);
}
