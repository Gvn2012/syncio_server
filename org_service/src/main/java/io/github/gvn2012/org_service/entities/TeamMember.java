package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "team_members",
        indexes = {
                @Index(name = "ix_team_members_team", columnList = "team_id"),
                @Index(name = "ix_team_members_user", columnList = "user_id"),
                @Index(name = "ix_team_members_active", columnList = "team_id, is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_team_members_team_user",
                        columnNames = {"team_id", "user_id"}
                )
        }
)
public class TeamMember extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** References User.id in user_service */
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false, length = 32)
    private TeamRole teamRole = TeamRole.MEMBER;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
