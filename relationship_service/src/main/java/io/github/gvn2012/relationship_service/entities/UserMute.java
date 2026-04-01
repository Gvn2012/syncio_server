package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.MuteScope;
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
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "user_mutes", indexes = {
        @Index(name = "ix_mute_muter", columnList = "muter_user_id"),
        @Index(name = "ix_mute_muted", columnList = "muted_user_id"),
        @Index(name = "ix_mute_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_mute_muter_muted_scope",
                columnNames = {"muter_user_id", "muted_user_id", "scope"}
        )
})
public class UserMute extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "muter_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID muterUserId;

    @NotNull
    @Column(name = "muted_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID mutedUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private MuteScope scope = MuteScope.ALL;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "unmuted_at")
    private LocalDateTime unmutedAt;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
