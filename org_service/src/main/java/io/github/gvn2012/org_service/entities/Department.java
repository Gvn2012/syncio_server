package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.DepartmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "departments",
        indexes = {
                @Index(name = "ix_departments_org", columnList = "organization_id"),
                @Index(name = "ix_departments_parent", columnList = "parent_department_id"),
                @Index(name = "ix_departments_head", columnList = "head_of_department_id"),
                @Index(name = "ix_departments_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_departments_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
public class Department extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** References User.id in user_service */
    @Column(name = "head_of_department_id", columnDefinition = "BINARY(16)")
    private UUID headOfDepartmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "cost_center_code", length = 64)
    private String costCenterCode;

    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL)
    private Set<Department> childDepartments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Team> teams = new LinkedHashSet<>();
}
