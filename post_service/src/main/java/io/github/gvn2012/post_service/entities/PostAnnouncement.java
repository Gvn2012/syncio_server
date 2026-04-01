package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "post_announcements", indexes = {
        @Index(name = "ix_post_ann_post_id", columnList = "post_id"),
        @Index(name = "ix_post_ann_priority", columnList = "priority")
})
public class PostAnnouncement {

    @Id
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "priority", nullable = false)
    private String priority = "NORMAL";

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "pinned_until")
    private LocalDateTime pinnedUntil;

    @Column(name = "requires_acknowledgement", nullable = false)
    private Boolean requiresAcknowledgement = false;

    @Column(name = "read_count", nullable = false)
    private Integer readCount = 0;
}