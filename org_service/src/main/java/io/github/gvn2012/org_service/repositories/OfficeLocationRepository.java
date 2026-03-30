package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.OfficeLocation;
import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, UUID> {

    List<OfficeLocation> findByOrganization_Id(UUID organizationId);

    List<OfficeLocation> findByOrganization_IdAndStatus(UUID organizationId, OfficeStatus status);

    Optional<OfficeLocation> findByOrganization_IdAndHeadquartersTrue(UUID organizationId);

    Optional<OfficeLocation> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
