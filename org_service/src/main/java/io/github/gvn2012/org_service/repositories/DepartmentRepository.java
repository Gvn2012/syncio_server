package io.github.gvn2012.org_service.repositories;

import io.github.gvn2012.org_service.entities.Department;
import io.github.gvn2012.org_service.entities.enums.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByOrganization_Id(UUID organizationId);

    List<Department> findByOrganization_IdAndStatus(UUID organizationId, DepartmentStatus status);

    List<Department> findByParentDepartment_Id(UUID parentDepartmentId);

    Optional<Department> findByOrganization_IdAndCode(UUID organizationId, String code);

    Optional<Department> findByIdAndOrganization_Id(UUID id, UUID organizationId);
}
