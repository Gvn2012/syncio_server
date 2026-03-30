package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.EmploymentStatus;
import io.github.gvn2012.user_service.entities.enums.EmploymentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_employments",
        indexes = {
                @Index(name = "ix_user_employments_user", columnList = "user_id"),
                @Index(name = "ix_user_employments_org", columnList = "organization_id"),
                @Index(name = "ix_user_employments_dept", columnList = "department_id"),
                @Index(name = "ix_user_employments_user_current", columnList = "user_id, is_current"),
                @Index(name = "ix_user_employments_status", columnList = "employment_status"),
                @Index(name = "ix_user_employments_manager", columnList = "manager_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_employments_employee_code_org",
                        columnNames = {"employee_code", "organization_id"}
                )
        }
)
public class UserEmployment extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** References Organization.id in org_service */
    @Column(name = "organization_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID organizationId;

    /** References Department.id in org_service */
    @Column(name = "department_id", columnDefinition = "BINARY(16)")
    private UUID departmentId;

    /** References Team.id in org_service */
    @Column(name = "team_id", columnDefinition = "BINARY(16)")
    private UUID teamId;

    @Column(name = "employee_code", length = 64)
    @Size(max = 64)
    private String employeeCode;

    /**
     * References OrgJobTitle.id in org_service.
     * Job titles are org-defined custom fields, not hardcoded enums.
     */
    @Column(name = "job_title_id", columnDefinition = "BINARY(16)")
    private UUID jobTitleId;

    /**
     * References OrgJobLevel.id in org_service.
     * Job levels are org-defined custom fields, not hardcoded enums.
     */
    @Column(name = "job_level_id", columnDefinition = "BINARY(16)")
    private UUID jobLevelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 32)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    /** References User.id — the user's direct manager */
    @Column(name = "manager_id", columnDefinition = "BINARY(16)")
    private UUID managerId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "work_location")
    @Size(max = 255)
    private String workLocation;

    @Column(name = "is_current", nullable = false)
    private Boolean current = true;
}
