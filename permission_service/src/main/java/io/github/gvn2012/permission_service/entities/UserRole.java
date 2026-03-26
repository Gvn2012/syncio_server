package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.RoleAssignmentStatus;
import io.github.gvn2012.permission_service.entities.enums.RoleScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "user_roles", indexes = {
        @Index(name = "ix_user_role_user", columnList = "user_id"),
        @Index(name = "ix_user_role_role", columnList = "role_id"),
        @Index(name = "ix_user_role_scope", columnList = "scope, scope_id"),
        @Index(name = "ix_user_role_status", columnList = "status"),
        @Index(name = "ix_user_role_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_role_scope",
                columnNames = {"user_id", "role_id", "scope", "scope_id"}
        )
})
public class UserRole extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private RoleScope scope = RoleScope.GLOBAL;

    @Column(name = "scope_id", columnDefinition = "BINARY(16)")
    private UUID scopeId; // group_id, channel_id, organization_id when scope is not GLOBAL

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoleAssignmentStatus status = RoleAssignmentStatus.ACTIVE;

    @Column(name = "assigned_by", columnDefinition = "BINARY(16)")
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by", columnDefinition = "BINARY(16)")
    private UUID revokedBy;

    @Column(name = "revoke_reason", columnDefinition = "VARCHAR(512)")
    private String revokeReason;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // primary role for display

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == RoleAssignmentStatus.ACTIVE && !isExpired();
    }
}
