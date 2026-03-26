package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.PermissionEffect;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "resource_permissions", indexes = {
        @Index(name = "ix_res_perm_resource", columnList = "resource_type, resource_id"),
        @Index(name = "ix_res_perm_principal", columnList = "principal_type, principal_id"),
        @Index(name = "ix_res_perm_expires", columnList = "expires_at")
})
public class ResourcePermission extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    // Resource being protected
    @NotBlank
    @Size(max = 64)
    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType; // "post", "group", "document"

    @NotNull
    @Column(name = "resource_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID resourceId;

    // Principal receiving permission
    @NotBlank
    @Size(max = 32)
    @Column(name = "principal_type", nullable = false, length = 32)
    private String principalType; // "user", "role", "group", "everyone"

    @Column(name = "principal_id", columnDefinition = "BINARY(16)")
    private UUID principalId; // null for "everyone"

    // Permission details
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false)
    private PermissionEffect effect = PermissionEffect.ALLOW;

    // Metadata
    @Column(name = "granted_by", columnDefinition = "BINARY(16)")
    private UUID grantedBy;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_inherited", nullable = false)
    private Boolean isInherited = false; // inherited from parent resource

    @Column(name = "source_resource_id", columnDefinition = "BINARY(16)")
    private UUID sourceResourceId; // if inherited, the source

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
