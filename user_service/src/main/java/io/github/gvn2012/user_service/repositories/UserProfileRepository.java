package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    boolean existsByUser_Id(UUID userId);
}
