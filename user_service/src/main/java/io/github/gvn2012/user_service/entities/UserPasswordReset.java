package io.github.gvn2012.user_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_password_reset",
        indexes = {
                @Index(name = "ix_pwreset_user_created", columnList = "user_id, created_at"),
                @Index(name = "ix_pwreset_user_expires", columnList = "user_id, expires_at")
        }
)
public class UserPasswordReset extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 512)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    @Column(name = "request_user_agent", length = 512)
    private String requestUserAgent;
}