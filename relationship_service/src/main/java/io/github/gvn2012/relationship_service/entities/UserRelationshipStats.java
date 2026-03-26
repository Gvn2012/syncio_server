package io.github.gvn2012.relationship_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "user_relationship_stats")
public class UserRelationshipStats extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Min(0)
    @Column(name = "follower_count", nullable = false)
    private Long followerCount = 0L;

    @Min(0)
    @Column(name = "following_count", nullable = false)
    private Long followingCount = 0L;

    @Min(0)
    @Column(name = "friend_count", nullable = false)
    private Long friendCount = 0L;

    @Min(0)
    @Column(name = "close_friend_count", nullable = false)
    private Long closeFriendCount = 0L;

    @Min(0)
    @Column(name = "pending_friend_requests_in", nullable = false)
    private Long pendingFriendRequestsIn = 0L;

    @Min(0)
    @Column(name = "pending_friend_requests_out", nullable = false)
    private Long pendingFriendRequestsOut = 0L;

    @Min(0)
    @Column(name = "blocked_count", nullable = false)
    private Long blockedCount = 0L;

    @Min(0)
    @Column(name = "blocked_by_count", nullable = false)
    private Long blockedByCount = 0L;

    @Min(0)
    @Column(name = "muted_count", nullable = false)
    private Long mutedCount = 0L;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;
}
