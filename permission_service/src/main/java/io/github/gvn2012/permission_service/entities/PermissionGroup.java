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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "permission_groups", uniqueConstraints = {
                @UniqueConstraint(name = "uk_perm_group_code", columnNames = "code")
})
public class PermissionGroup extends AuditableEntity {

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
        private String code; // e.g., "CONTENT_MANAGEMENT", "USER_MANAGEMENT"

        @NotBlank
        @Size(max = 128)
        @Column(name = "name", nullable = false, length = 128)
        private String name;

        @Size(max = 512)
        @Column(name = "description", length = 512)
        private String description;

        @Column(name = "display_order")
        private Integer displayOrder;

        @Column(name = "icon", length = 64)
        private String icon;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "permission_group_permissions", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
        private Set<Permission> permissions = new LinkedHashSet<>();
}
