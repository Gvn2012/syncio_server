package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.InvitationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "org_invitations",
        indexes = {
                @Index(name = "ix_org_invitations_org", columnList = "organization_id"),
                @Index(name = "ix_org_invitations_email", columnList = "invited_email"),
                @Index(name = "ix_org_invitations_status", columnList = "status"),
                @Index(name = "ix_org_invitations_expires", columnList = "expires_at")
        }
)
public class OrgInvitation extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "invited_email", nullable = false)
    @NotBlank
    @Email
    private String invitedEmail;

    /** References User.id in user_service */
    @Column(name = "invited_by_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID invitedByUserId;

    /** Optional — pre-assign to department */
    @Column(name = "department_id", columnDefinition = "BINARY(16)")
    private UUID departmentId;

    /** Optional — pre-assign to position */
    @Column(name = "position_id", columnDefinition = "BINARY(16)")
    private UUID positionId;

    @Column(name = "token_hash", nullable = false, length = 512)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    /** References User.id — the user who accepted */
    @Column(name = "accepted_by_user_id", columnDefinition = "BINARY(16)")
    private UUID acceptedByUserId;
}
