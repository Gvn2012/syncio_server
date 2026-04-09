package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "roles", indexes = {
                @Index(name = "ix_role_type", columnList = "role_type"),
                @Index(name = "ix_role_hierarchy", columnList = "hierarchy_level")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_role_code", columnNames = "code")
})
public class Role extends AuditableEntity {

        @Id
        @EqualsAndHashCode.Include
        @ToString.Include
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false, updatable = false)
        private Integer id;

        @NotBlank
        @Size(max = 64)
        @ToString.Include
        @Column(name = "code", nullable = false, length = 64)
        private String code; // e.g., "ADMIN", "MODERATOR", "USER", "GUEST"

        @NotBlank
        @Size(max = 128)
        @Column(name = "name", nullable = false, length = 128)
        private String name;

        @Size(max = 512)
        @Column(name = "description", length = 512)
        private String description;

        @Builder.Default
        @Enumerated(EnumType.STRING)
        @Column(name = "role_type", nullable = false)
        private RoleType roleType = RoleType.CUSTOM;

        @Builder.Default
        @Column(name = "hierarchy_level", nullable = false)
        private Integer hierarchyLevel = 0; // higher = more privileges, for inheritance

        @Builder.Default
        @Column(name = "is_system", nullable = false)
        private Boolean isSystem = false;

        @Builder.Default
        @Column(name = "is_default", nullable = false)
        private Boolean isDefault = false; // assigned to new users

        @Builder.Default
        @Column(name = "is_active", nullable = false)
        private Boolean isActive = true;

        @Builder.Default
        @Column(name = "is_assignable", nullable = false)
        private Boolean isAssignable = true; // can be assigned by admins

        @Column(name = "max_users")
        private Integer maxUsers; // limit users with this role (e.g., only 5 super admins)

        @Column(name = "color", length = 7)
        private String color; // hex color for UI badge

        @Column(name = "icon", length = 64)
        private String icon;

        @Column(name = "display_order")
        private Integer displayOrder;

        // ================= RELATIONSHIPS =================

        @ToString.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_role_id")
        private Role parentRole; // for role inheritance

        @Builder.Default
        @OneToMany(mappedBy = "parentRole", fetch = FetchType.LAZY)
        private Set<Role> childRoles = new LinkedHashSet<>();

        @Builder.Default
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"), uniqueConstraints = @UniqueConstraint(name = "uk_role_permission", columnNames = {
                        "role_id", "permission_id" }))
        private Set<Permission> permissions = new LinkedHashSet<>();

        @Builder.Default
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "role_included_roles", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "included_role_id"), uniqueConstraints = @UniqueConstraint(name = "uk_role_included", columnNames = {
                        "role_id", "included_role_id" }))
        private Set<Role> includedRoles = new LinkedHashSet<>(); // composite roles
}
