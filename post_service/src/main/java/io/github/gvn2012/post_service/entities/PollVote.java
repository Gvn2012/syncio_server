package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "poll_votes",
        indexes = {
                @Index(name = "ix_poll_vote_poll", columnList = "poll_id"),
                @Index(name = "ix_poll_vote_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_poll_vote_user_option",
                        columnNames = {"poll_id", "user_id", "option_id"}
                )
        }
)
public class PollVote {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private PostPoll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private PollOption option;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;
}