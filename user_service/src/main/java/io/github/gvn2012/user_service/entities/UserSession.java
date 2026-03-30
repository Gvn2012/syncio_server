package io.github.gvn2012.user_service.entities;

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
        name = "user_sessions",
        indexes = {
                @Index(name = "ix_user_sessions_user", columnList = "user_id"),
                @Index(name = "ix_user_sessions_user_active", columnList = "user_id, is_revoked"),
                @Index(name = "ix_user_sessions_expires", columnList = "expires_at")
        }
)
public class UserSession extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_token_hash", nullable = false, length = 512)
    private String sessionTokenHash;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    @Size(max = 512)
    private String userAgent;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "device_name", length = 128)
    private String deviceName;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private Boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_reason")
    @Size(max = 255)
    private String revokedReason;
}
