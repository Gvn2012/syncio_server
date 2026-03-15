package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.OAuthProvider;
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
        name = "oauth_accounts",
        indexes = {
                @Index(name = "ix_oauth_user", columnList = "user_id"),
                @Index(name = "ix_oauth_provider_email", columnList = "provider_email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_oauth_provider_user", columnNames = {"provider", "provider_user_id"})
        }
)
public class OAuthAccount extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 64)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @Lob
    @Column(name = "access_token")
    private String accessToken;

    @Lob
    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
}