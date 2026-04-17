package io.github.gvn2012.permission_service.entities;

import io.github.gvn2012.permission_service.entities.enums.PermissionAuditAction;
import io.github.gvn2012.permission_service.entities.enums.PermissionDecision;
import jakarta.persistence.*;
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
@Table(name = "permission_audit_log", indexes = {
        @Index(name = "ix_perm_audit_user", columnList = "user_id"),
        @Index(name = "ix_perm_audit_resource", columnList = "resource_type, resource_id"),
        @Index(name = "ix_perm_audit_time", columnList = "created_at"),
        @Index(name = "ix_perm_audit_action", columnList = "audit_action"),
        @Index(name = "ix_perm_audit_decision", columnList = "decision")
})
public class PermissionAuditLog {

    @Id
    @EqualsAndHashCode.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "audit_action", nullable = false)
    private PermissionAuditAction auditAction;

    // Who
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "actor_id", columnDefinition = "BINARY(16)")
    private UUID actorId; // admin who made the change (for management actions)

    // What
    @Size(max = 64)
    @Column(name = "resource_type", length = 64)
    private String resourceType;

    @Column(name = "resource_id", columnDefinition = "BINARY(16)")
    private UUID resourceId;

    @Size(max = 128)
    @Column(name = "permission_code", length = 128)
    private String permissionCode;

    @Size(max = 64)
    @Column(name = "action_attempted", length = 64)
    private String actionAttempted;

    // Decision
    @Enumerated(EnumType.STRING)
    @Column(name = "decision")
    private PermissionDecision decision;

    @Column(name = "decision_reason", columnDefinition = "VARCHAR(512)")
    private String decisionReason;

    @Column(name = "policies_evaluated", columnDefinition = "json")
    private String policiesEvaluated; // list of policy IDs that were checked

    @Column(name = "matching_policy_id", columnDefinition = "BINARY(16)")
    private UUID matchingPolicyId;

    // Changes (for management actions)
    @Column(name = "previous_state", columnDefinition = "json")
    private String previousState;

    @Column(name = "new_state", columnDefinition = "json")
    private String newState;

    // Context
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "session_id", length = 128)
    private String sessionId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "service_name", length = 64)
    private String serviceName; // which service made the check

    // Timing
    @Column(name = "evaluation_time_ms")
    private Integer evaluationTimeMs;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
