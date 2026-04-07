package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfilePictureRepository extends JpaRepository<UserProfilePicture, UUID> {
    Optional<UserProfilePicture> findByExternalId(String externalId);
}
