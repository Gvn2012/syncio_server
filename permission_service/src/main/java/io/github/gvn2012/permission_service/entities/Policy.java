package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.PermissionEffect;
import io.github.gvn2012.permission_service.entities.enums.PolicyCombiningAlgorithm;
import io.github.gvn2012.permission_service.entities.enums.PolicyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "policies", indexes = {
        @Index(name = "ix_policy_resource_action", columnList = "resource, action"),
        @Index(name = "ix_policy_status", columnList = "status"),
        @Index(name = "ix_policy_priority", columnList = "priority DESC")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_policy_code", columnNames = "code")
})
public class Policy extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank
    @Size(max = 128)
    @ToString.Include
    @Column(name = "code", nullable = false, length = 128)
    private String code; // e.g., "post-visibility-policy"

    @NotBlank
    @Size(max = 256)
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Size(max = 1024)
    @Column(name = "description", length = 1024)
    private String description;

    @Size(max = 64)
    @Column(name = "resource", length = 64)
    private String resource; // "*" for all resources

    @Size(max = 64)
    @Column(name = "action", length = 64)
    private String action; // "*" for all actions

    @Enumerated(EnumType.STRING)
    @Column(name = "effect", nullable = false)
    private PermissionEffect effect;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PolicyStatus status = PolicyStatus.DRAFT;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0; // higher = evaluated first

    @Enumerated(EnumType.STRING)
    @Column(name = "combining_algorithm", nullable = false)
    private PolicyCombiningAlgorithm combiningAlgorithm = PolicyCombiningAlgorithm.DENY_OVERRIDES;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    // ================= RELATIONSHIPS =================

    @ToString.Exclude
    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("conditionOrder ASC")
    private Set<PolicyCondition> conditions = new LinkedHashSet<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_policy_id")
    private Policy parentPolicy; // for policy sets

    @ToString.Exclude
    @OneToMany(mappedBy = "parentPolicy", fetch = FetchType.LAZY)
    private Set<Policy> childPolicies = new LinkedHashSet<>();
}
