package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.BlockReason;
import io.github.gvn2012.relationship_service.entities.enums.BlockScope;
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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "user_blocks", indexes = {
        @Index(name = "ix_block_blocker", columnList = "blocker_user_id"),
        @Index(name = "ix_block_blocked", columnList = "blocked_user_id"),
        @Index(name = "ix_block_active", columnList = "blocker_user_id, is_active")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_block_blocker_blocked",
                columnNames = {"blocker_user_id", "blocked_user_id"}
        )
})
public class UserBlock extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "blocker_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID blockerUserId;

    @NotNull
    @Column(name = "blocked_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID blockedUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private BlockScope scope = BlockScope.FULL;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private BlockReason reason;

    @Column(name = "reason_note", columnDefinition = "VARCHAR(512)")
    private String reasonNote;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "unblocked_at")
    private LocalDateTime unblockedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // temporary block

    @Column(name = "block_count", nullable = false)
    private Integer blockCount = 1; // times this user has been blocked

    // ================= VALIDATION =================

    @PrePersist
    @PreUpdate
    private void validate() {
        if (blockerUserId.equals(blockedUserId)) {
            throw new IllegalArgumentException("Cannot block self");
        }
    }
}
