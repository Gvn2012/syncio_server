package io.github.gvn2012.user_service.repositories;

import io.github.gvn2012.user_service.entities.UserEmployment;
import io.github.gvn2012.user_service.entities.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEmploymentRepository extends JpaRepository<UserEmployment, UUID> {

    List<UserEmployment> findByUser_Id(UUID userId);

    List<UserEmployment> findByUser_IdAndCurrentTrue(UUID userId);

    Optional<UserEmployment> findByIdAndUser_Id(UUID id, UUID userId);

    List<UserEmployment> findByOrganizationId(UUID organizationId);

    List<UserEmployment> findByOrganizationIdAndDepartmentId(UUID organizationId, UUID departmentId);

    List<UserEmployment> findByOrganizationIdAndEmploymentStatus(UUID organizationId, EmploymentStatus status);

    List<UserEmployment> findByManagerId(UUID managerId);

    boolean existsByEmployeeCodeAndOrganizationId(String employeeCode, UUID organizationId);
}
