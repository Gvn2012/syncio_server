package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.OrgAuditAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "org_audit_logs",
        indexes = {
                @Index(name = "ix_org_audit_logs_org", columnList = "organization_id"),
                @Index(name = "ix_org_audit_logs_actor", columnList = "actor_user_id"),
                @Index(name = "ix_org_audit_logs_action", columnList = "action"),
                @Index(name = "ix_org_audit_logs_performed_at", columnList = "performed_at")
        }
)
public class OrgAuditLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID organizationId;

    /** References User.id — who performed the action */
    @Column(name = "actor_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private OrgAuditAction action;

    @Column(name = "target_entity_type", length = 64)
    private String targetEntityType;

    @Column(name = "target_entity_id", length = 64)
    @Size(max = 64)
    private String targetEntityId;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @PrePersist
    protected void onPersist() {
        if (performedAt == null) {
            performedAt = Instant.now();
        }
    }
}
