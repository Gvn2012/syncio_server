package io.github.gvn2012.org_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Organization-defined skill definitions.
 * Orgs can define their own skill catalog, which employees
 * can then claim/be verified against via UserSkill.skillDefinitionId in user_service.
 */
@Getter
@Setter
@Entity
@Table(
        name = "org_skill_definitions",
        indexes = {
                @Index(name = "ix_org_skill_defs_org", columnList = "organization_id"),
                @Index(name = "ix_org_skill_defs_category", columnList = "category"),
                @Index(name = "ix_org_skill_defs_active", columnList = "organization_id, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_skill_defs_org_code",
                        columnNames = {"organization_id", "code"}
                )
        }
)
public class OrgSkillDefinition extends AuditableEntity {

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

    /** Grouping category, e.g., "Technical", "Soft Skills", "Management" */
    @Column(name = "category", length = 64)
    @Size(max = 64)
    private String category;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
