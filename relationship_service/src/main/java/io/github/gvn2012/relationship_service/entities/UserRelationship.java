package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.RelationshipStatus;
import io.github.gvn2012.relationship_service.entities.enums.RelationshipType;
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
@Table(name = "user_relationships", indexes = {
        @Index(name = "ix_rel_source_type_status", columnList = "source_user_id, relationship_type, status"),
        @Index(name = "ix_rel_target_type_status", columnList = "target_user_id, relationship_type, status"),
        @Index(name = "ix_rel_created_at", columnList = "created_at"),
        @Index(name = "ix_rel_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_relationship_source_target_type",
                columnNames = {"source_user_id", "target_user_id", "relationship_type"}
        )
})
public class UserRelationship extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "source_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID sourceUserId; // the user initiating the relationship

    @NotNull
    @Column(name = "target_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID targetUserId; // the user being followed/friended

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, updatable = false)
    private RelationshipType relationshipType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RelationshipStatus status = RelationshipStatus.ACTIVE;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt; // for friend requests

    @Column(name = "source_nickname", length = 64)
    private String sourceNickname; // custom nickname source gives to target

    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Column(name = "show_in_feed", nullable = false)
    private Boolean showInFeed = true; // mute without unfollowing

    @Column(name = "is_close_friend", nullable = false)
    private Boolean isCloseFriend = false;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "interaction_score")
    private Double interactionScore = 0.0; // calculated engagement level

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    // ================= VALIDATION =================

    @PreUpdate
    private void validate() {
        if (sourceUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot create relationship with self");
        }
    }
}
