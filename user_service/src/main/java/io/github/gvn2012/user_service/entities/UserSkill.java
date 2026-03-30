package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.ProficiencyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_skills",
        indexes = {
                @Index(name = "ix_user_skills_user", columnList = "user_id"),
                @Index(name = "ix_user_skills_skill_def", columnList = "skill_definition_id"),
                @Index(name = "ix_user_skills_user_verified", columnList = "user_id, is_verified")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_skills_user_skill_def",
                        columnNames = {"user_id", "skill_definition_id"}
                )
        }
)
public class UserSkill extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * References OrgSkillDefinition.id in org_service.
     * Skill definitions are org-defined custom fields.
     * Null if the skill is user-defined (freeform).
     */
    @Column(name = "skill_definition_id", columnDefinition = "BINARY(16)")
    private UUID skillDefinitionId;

    /** Freeform skill name — used when skillDefinitionId is null */
    @Column(name = "skill_name")
    @Size(max = 128)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", length = 32)
    private ProficiencyLevel proficiencyLevel;

    @Column(name = "years_of_experience")
    @Min(0)
    private Integer yearsOfExperience;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    /** References User.id — who verified this skill */
    @Column(name = "verified_by", columnDefinition = "BINARY(16)")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
