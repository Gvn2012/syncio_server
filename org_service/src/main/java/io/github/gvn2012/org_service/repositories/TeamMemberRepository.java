package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    List<TeamMember> findByTeam_IdAndActiveTrue(UUID teamId);

    List<TeamMember> findByUserId(UUID userId);

    List<TeamMember> findByUserIdAndActiveTrue(UUID userId);

    Optional<TeamMember> findByTeam_IdAndUserId(UUID teamId, UUID userId);

    long countByTeam_IdAndActiveTrue(UUID teamId);
}
