package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    List<Position> findByOrganization_IdAndActiveTrue(UUID organizationId);

    List<Position> findByOrganization_IdAndDepartment_Id(UUID organizationId, UUID departmentId);

    Optional<Position> findByOrganization_IdAndCode(UUID organizationId, String code);

    Optional<Position> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
