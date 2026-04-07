package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.enums.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findBySlug(String slug);

    List<Organization> findByOwnerId(UUID ownerId);

    List<Organization> findByStatus(OrganizationStatus status);

    List<Organization> findByParentOrganizationId(UUID parentOrgId);

    boolean existsBySlug(String slug);

    Boolean existsByName(@NotBlank @Size(max = 255) String name);
}
