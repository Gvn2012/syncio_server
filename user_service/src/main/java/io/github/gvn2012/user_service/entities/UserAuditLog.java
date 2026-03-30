package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.UserAuditAction;
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
        name = "user_audit_logs",
        indexes = {
                @Index(name = "ix_user_audit_logs_user", columnList = "user_id"),
                @Index(name = "ix_user_audit_logs_action", columnList = "action"),
                @Index(name = "ix_user_audit_logs_performed_at", columnList = "performed_at"),
                @Index(name = "ix_user_audit_logs_user_action", columnList = "user_id, action")
        }
)
public class UserAuditLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    /** Stored as bare UUID — not a FK. Audit logs are immutable records. */
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private UserAuditAction action;

    @Column(name = "target_entity_type", length = 64)
    private String targetEntityType;

    @Column(name = "target_entity_id", length = 64)
    private String targetEntityId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    @Size(max = 512)
    private String userAgent;

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
