package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.TeamStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "teams",
        indexes = {
                @Index(name = "ix_teams_department", columnList = "department_id"),
                @Index(name = "ix_teams_lead", columnList = "team_lead_id"),
                @Index(name = "ix_teams_status", columnList = "status")
        }
)
public class Team extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** References User.id in user_service */
    @Column(name = "team_lead_id", columnDefinition = "BINARY(16)")
    private UUID teamLeadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TeamStatus status = TeamStatus.ACTIVE;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMember> members = new LinkedHashSet<>();
}
