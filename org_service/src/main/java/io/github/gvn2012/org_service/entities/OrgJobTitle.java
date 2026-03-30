package io.github.gvn2012.org_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Organization-defined job titles.
 * Instead of hardcoded enums, each org can define its own job title catalog.
 * Referenced by UserEmployment.jobTitleId in user_service.
 */
@Getter
@Setter
@Entity
@Table(
        name = "org_job_titles",
        indexes = {
                @Index(name = "ix_org_job_titles_org", columnList = "organization_id"),
                @Index(name = "ix_org_job_titles_active", columnList = "organization_id, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_job_titles_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
public class OrgJobTitle extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

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

    /** Optional — link to a specific department if this title is department-scoped */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
