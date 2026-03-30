package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.Team;
import io.github.gvn2012.org_service.entities.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByDepartment_Id(UUID departmentId);

    List<Team> findByDepartment_IdAndStatus(UUID departmentId, TeamStatus status);

    Optional<Team> findByIdAndDepartment_Id(UUID id, UUID departmentId);
}
