package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.RelationshipAuditAction;
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
@Table(name = "relationship_audit_log", indexes = {
        @Index(name = "ix_rel_audit_user", columnList = "actor_user_id"),
        @Index(name = "ix_rel_audit_target", columnList = "target_user_id"),
        @Index(name = "ix_rel_audit_time", columnList = "created_at"),
        @Index(name = "ix_rel_audit_action", columnList = "action")
})
public class RelationshipAuditLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "actor_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID actorUserId;

    @NotNull
    @Column(name = "target_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID targetUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RelationshipAuditAction action;

    @Column(name = "relationship_id", columnDefinition = "BINARY(16)")
    private UUID relationshipId;

    @Column(name = "previous_state", columnDefinition = "json")
    private String previousState;

    @Column(name = "new_state", columnDefinition = "json")
    private String newState;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
