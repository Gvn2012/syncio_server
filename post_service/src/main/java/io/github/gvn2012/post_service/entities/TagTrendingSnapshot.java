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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tag_trending_snapshots", indexes = {
        @Index(name = "ix_tag_snapshot_tag_time", columnList = "tag_id, snapshot_time"),
        @Index(name = "ix_tag_snapshot_time", columnList = "snapshot_time")
})
public class TagTrendingSnapshot {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;

    @Column(name = "post_count_1h", nullable = false)
    private Long postCount1h;

    @Column(name = "trending_score", nullable = false)
    private Double trendingScore;

    @Column(name = "trend_rank")
    private Integer trendRank;

    @Column(name = "is_trending", nullable = false)
    private Boolean isTrending;
}
