package io.github.gvn2012.org_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Organization-defined job levels / grades.
 * Instead of hardcoded enums (INTERN, JUNIOR, SENIOR, etc.),
 * each org can define its own grading system.
 * Referenced by UserEmployment.jobLevelId in user_service.
 */
@Getter
@Setter
@Entity
@Table(
        name = "org_job_levels",
        indexes = {
                @Index(name = "ix_org_job_levels_org", columnList = "organization_id"),
                @Index(name = "ix_org_job_levels_active", columnList = "organization_id, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_job_levels_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
public class OrgJobLevel extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 128)
    private String name;

    @Column(name = "code", nullable = false, length = 32)
    @NotBlank
    @Size(max = 32)
    private String code;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    /**
     * Numeric rank for ordering / comparison.
     * e.g., L1=1, L2=2, ... L10=10
     */
    @Column(name = "rank_order", nullable = false)
    @Min(0)
    private Integer rankOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
