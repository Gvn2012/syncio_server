package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.ConditionOperator;
import io.github.gvn2012.permission_service.entities.enums.ConditionSubjectType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "policy_conditions", indexes = {
        @Index(name = "ix_policy_condition_policy", columnList = "policy_id"),
        @Index(name = "ix_policy_condition_order", columnList = "policy_id, condition_order")
})
public class PolicyCondition extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    private ConditionSubjectType subjectType;

    @NotBlank
    @Size(max = 128)
    @Column(name = "attribute", nullable = false, length = 128)
    private String attribute; // e.g., "user.role", "resource.owner_id", "environment.time"

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private ConditionOperator operator;

    @NotBlank
    @Column(name = "value", nullable = false, columnDefinition = "VARCHAR(1024)")
    private String value; // can be JSON for complex values

    @Column(name = "value_type", length = 32)
    private String valueType; // "string", "number", "boolean", "array", "regex"

    @Column(name = "condition_order", nullable = false)
    private Integer conditionOrder = 0;

    @Column(name = "logical_operator", length = 8)
    private String logicalOperator = "AND"; // AND, OR for chaining conditions

    @Column(name = "is_negated", nullable = false)
    private Boolean isNegated = false; // NOT operator

    @Size(max = 256)
    @Column(name = "description", length = 256)
    private String description;
}
