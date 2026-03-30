package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserPreference;
import io.github.gvn2012.user_service.entities.enums.PreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    List<UserPreference> findByUser_Id(UUID userId);

    List<UserPreference> findByUser_IdAndCategory(UUID userId, PreferenceCategory category);

    Optional<UserPreference> findByUser_IdAndPreferenceKey(UUID userId, String preferenceKey);

    Optional<UserPreference> findByIdAndUser_Id(UUID id, UUID userId);

    void deleteByUser_IdAndPreferenceKey(UUID userId, String preferenceKey);
}
