package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.PermissionEffect;
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
@Table(name = "user_permission_overrides", indexes = {
        @Index(name = "ix_perm_override_user", columnList = "user_id"),
        @Index(name = "ix_perm_override_permission", columnList = "permission_id"),
        @Index(name = "ix_perm_override_scope", columnList = "scope, scope_id"),
        @Index(name = "ix_perm_override_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_permission_scope",
                columnNames = {"user_id", "permission_id", "scope", "scope_id"}
        )
})
public class UserPermissionOverride extends AuditableEntity {

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
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false)
    private PermissionEffect effect; // ALLOW or DENY

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private RoleScope scope = RoleScope.GLOBAL;

    @Column(name = "scope_id", columnDefinition = "BINARY(16)")
    private UUID scopeId;

    @Column(name = "resource_id", columnDefinition = "BINARY(16)")
    private UUID resourceId; // specific resource this override applies to

    @Column(name = "reason", columnDefinition = "VARCHAR(512)")
    private String reason;

    @Column(name = "granted_by", columnDefinition = "BINARY(16)")
    private UUID grantedBy;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // higher priority overrides take precedence
}
