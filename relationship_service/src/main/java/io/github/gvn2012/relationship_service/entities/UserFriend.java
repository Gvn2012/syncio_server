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
@Table(name = "user_friends", indexes = {
        @Index(name = "ix_friend_user1_status", columnList = "user1_id, status"),
        @Index(name = "ix_friend_user2_status", columnList = "user2_id, status"),
        @Index(name = "ix_friend_accepted_at", columnList = "accepted_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_friend_user1_user2", columnNames = {"user1_id", "user2_id"})
})
public class UserFriend extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    // Canonical lower UUID of the friendship pair.
    @Column(name = "user1_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID user1Id;

    @NotNull
    // Canonical higher UUID of the friendship pair.
    @Column(name = "user2_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID user2Id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RelationshipStatus status = RelationshipStatus.ACTIVE;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "initiated_by_user_id", columnDefinition = "BINARY(16)")
    private UUID initiatedByUserId;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (user1Id != null && user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("Cannot create friendship with self");
        }
    }
}
