package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.SuggestionReason;
import io.github.gvn2012.relationship_service.entities.enums.SuggestionStatus;
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
@Table(name = "follow_suggestions", indexes = {
        @Index(name = "ix_suggestion_user_status", columnList = "user_id, status"),
        @Index(name = "ix_suggestion_score", columnList = "user_id, score DESC"),
        @Index(name = "ix_suggestion_expires", columnList = "expires_at")
}, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_suggestion_user_suggested",
                columnNames = {"user_id", "suggested_user_id"}
        )
})
public class FollowSuggestion extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId; // user receiving the suggestion

    @NotNull
    @Column(name = "suggested_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID suggestedUserId; // user being suggested

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_reason", nullable = false)
    private SuggestionReason primaryReason;

    @Column(name = "secondary_reasons", columnDefinition = "json")
    private String secondaryReasons; // ["MUTUAL_FRIENDS", "SIMILAR_INTERESTS"]

    @Column(name = "mutual_friend_count")
    private Integer mutualFriendCount;

    @Column(name = "mutual_friend_ids", columnDefinition = "json")
    private String mutualFriendIds; // sample for display

    @Column(name = "common_interests", columnDefinition = "json")
    private String commonInterests;

    @NotNull
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SuggestionStatus status = SuggestionStatus.ACTIVE;

    @Column(name = "is_seen", nullable = false)
    private Boolean isSeen = false;

    @Column(name = "seen_at")
    private LocalDateTime seenAt;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "followed_at")
    private LocalDateTime followedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "display_priority", nullable = false)
    private Integer displayPriority = 0;
}
