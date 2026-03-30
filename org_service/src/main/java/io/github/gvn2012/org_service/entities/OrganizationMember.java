package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.MembershipStatus;
import jakarta.persistence.*;
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
        name = "organization_members",
        indexes = {
                @Index(name = "ix_org_members_org", columnList = "organization_id"),
                @Index(name = "ix_org_members_user", columnList = "user_id"),
                @Index(name = "ix_org_members_status", columnList = "status"),
                @Index(name = "ix_org_members_org_status", columnList = "organization_id, status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_members_org_user",
                        columnNames = {"organization_id", "user_id"}
                )
        }
)
public class OrganizationMember extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** References User.id in user_service */
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    /** The org-level role label for this member (e.g., "Admin", "Manager", "Employee") */
    @Column(name = "org_role", length = 64)
    @Size(max = 64)
    private String orgRole;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    /** References User.id — who invited this member */
    @Column(name = "invited_by_user_id", columnDefinition = "BINARY(16)")
    private UUID invitedByUserId;
}
