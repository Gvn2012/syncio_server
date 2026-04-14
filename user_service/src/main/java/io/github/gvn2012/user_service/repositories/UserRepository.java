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
            "phones",
            "addresses",
            "emergencyContacts",
            "employments",
            "skills",
            "preferences"
    })
    @Transactional(readOnly = true)
    Optional<User> findDetailById(UUID userId);

    @EntityGraph(attributePaths = {
            "profile",
            "profile.pictures",
            "emails",
            "phones",
            "addresses",
            "emergencyContacts",
            "employments",
            "skills",
            "preferences"
    })
    @Transactional(readOnly = true)
    java.util.List<User> findDetailsByIdIn(java.util.Collection<UUID> userIds);

    @Transactional()
    Boolean existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse (String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.profile p " +
            "LEFT JOIN FETCH p.pictures pic " +
            "WHERE u.id IN :userIds AND (pic IS NULL OR pic.primary = true OR pic.deleted = false)")
    java.util.List<User> findSummariesByIdIn(@org.springframework.data.repository.query.Param("userIds") java.util.Collection<UUID> userIds);
}
