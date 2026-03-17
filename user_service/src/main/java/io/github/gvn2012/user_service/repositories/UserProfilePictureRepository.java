package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfilePictureRepository extends JpaRepository<UserProfilePicture, UUID> {
}
