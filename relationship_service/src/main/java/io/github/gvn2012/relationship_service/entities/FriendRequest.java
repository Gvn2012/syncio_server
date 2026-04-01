package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.FriendRequestStatus;
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
@Table(name = "friend_requests", indexes = {
        @Index(name = "ix_freq_sender", columnList = "sender_user_id, status"),
        @Index(name = "ix_freq_receiver", columnList = "receiver_user_id, status"),
        @Index(name = "ix_freq_status_created", columnList = "status, created_at"),
        @Index(name = "ix_freq_expires", columnList = "expires_at")
})
public class FriendRequest extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "sender_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID senderUserId;

    @NotNull
    @Column(name = "receiver_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID receiverUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendRequestStatus status = FriendRequestStatus.PENDING;

    @Column(name = "message", columnDefinition = "VARCHAR(512)")
    private String message; // optional message with request

    @Column(name = "mutual_friends_count")
    private Integer mutualFriendsCount;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "source", length = 64)
    private String source; // where the request originated: "search", "suggestion", "mutual_friend"

    @Column(name = "is_seen", nullable = false)
    private Boolean isSeen = false;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;
}
