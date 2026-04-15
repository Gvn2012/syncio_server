package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "poll_options", indexes = {
        @Index(name = "ix_poll_option_poll", columnList = "poll_id")
})
public class PollOption {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private PostPoll poll;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "position")
    private Integer position;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount = 0;
}