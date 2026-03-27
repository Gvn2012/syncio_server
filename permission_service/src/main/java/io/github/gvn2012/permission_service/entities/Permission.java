package io.github.gvn2012.permission_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "permissions", indexes = {
        @Index(name = "ix_permission_resource_action", columnList = "resource, action"),
        @Index(name = "ix_permission_code", columnList = "code")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_code", columnNames = "code")
})
public class Permission extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Size(max = 128)
    @ToString.Include
    @Column(name = "code", nullable = false, length = 128)
    private String code; // e.g., "post:create", "user:delete", "comment:moderate"

    @NotBlank
    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name; // human-readable name

    @Size(max = 512)
    @Column(name = "description", length = 512)
    private String description;

    @NotBlank
    @Size(max = 64)
    @Column(name = "resource", nullable = false, length = 64)
    private String resource; // e.g., "post", "user", "comment", "group"

    @NotBlank
    @Size(max = 64)
    @Column(name = "action", nullable = false, length = 64)
    private String action; // e.g., "create", "read", "update", "delete", "moderate"

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false; // system permissions cannot be deleted

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "requires_ownership", nullable = false)
    private Boolean requiresOwnership = false; // must own resource to have permission

    @Column(name = "requires_relationship", nullable = false)
    private Boolean requiresRelationship = false; // must have relationship with resource owner

    @Size(max = 64)
    @Column(name = "category", length = 64)
    private String category; // for grouping in UI: "content", "moderation", "admin"

    @Column(name = "risk_level", nullable = false)
    private Integer riskLevel = 1; // 1-5, for audit prioritization

    @Column(name = "display_order")
    private Integer displayOrder;

    @ToString.Exclude
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new LinkedHashSet<>();
}
