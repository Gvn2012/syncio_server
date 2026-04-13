package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
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
@Table(name = "user_follows", indexes = {
        @Index(name = "ix_follow_follower_status", columnList = "follower_user_id, status"),
        @Index(name = "ix_follow_followee_status", columnList = "followee_user_id, status"),
        @Index(name = "ix_follow_created_at", columnList = "created_at")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_follow_follower_followee",
                columnNames = {"follower_user_id", "followee_user_id"}
        )
})
public class UserFollow extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "follower_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID followerUserId;

    @NotNull
    @Column(name = "followee_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID followeeUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RelationshipStatus status = RelationshipStatus.ACTIVE;

    @Column(name = "source_nickname", length = 64)
    private String sourceNickname;

    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Column(name = "show_in_feed", nullable = false)
    private Boolean showInFeed = true;

    @Column(name = "interaction_score")
    private Double interactionScore = 0.0;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (followerUserId != null && followerUserId.equals(followeeUserId)) {
            throw new IllegalArgumentException("Cannot follow self");
        }
    }
}
