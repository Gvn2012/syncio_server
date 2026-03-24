package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "post_polls", indexes = {
        @Index(name = "ix_post_polls_post_id", columnList = "post_id"),
        @Index(name = "ix_post_polls_expire", columnList = "expires_at")
})
public class PostPoll {

    @Id
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "allows_multiple_answers", nullable = false)
    private Boolean allowsMultipleAnswers = false;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes = 0;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PollOption> options = new LinkedHashSet<>();
}