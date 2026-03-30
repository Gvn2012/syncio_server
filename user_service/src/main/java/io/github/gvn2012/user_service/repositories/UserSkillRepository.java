package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUser_Id(UUID userId);

    List<UserSkill> findByUser_IdAndVerifiedTrue(UUID userId);

    Optional<UserSkill> findByIdAndUser_Id(UUID id, UUID userId);

    boolean existsByUser_IdAndSkillDefinitionId(UUID userId, UUID skillDefinitionId);

    List<UserSkill> findBySkillDefinitionId(UUID skillDefinitionId);
}
