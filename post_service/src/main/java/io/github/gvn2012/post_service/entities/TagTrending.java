package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "tag_trending", indexes = {
        @Index(name = "ix_tag_trending_score", columnList = "trending_score DESC"),
        @Index(name = "ix_tag_trending_rank", columnList = "trend_rank"),
        @Index(name = "ix_tag_trending_is_trending", columnList = "is_trending, trending_score DESC")
})
public class TagTrending extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "tag_id", nullable = false, updatable = false)
    private Integer tagId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "tag_id")
    private Tag tag;

    // ================= TIME-WINDOW COUNTS =================

    @Builder.Default
    @Column(name = "post_count_1h", nullable = false)
    private Long postCount1h = 0L;

    @Builder.Default
    @Column(name = "post_count_6h", nullable = false)
    private Long postCount6h = 0L;

    @Builder.Default
    @Column(name = "post_count_24h", nullable = false)
    private Long postCount24h = 0L;

    @Builder.Default
    @Column(name = "post_count_7d", nullable = false)
    private Long postCount7d = 0L;

    // ================= VELOCITY (rate of change) =================

    @Column(name = "velocity_1h")
    private Double velocity1h; // (count_1h - previous_count_1h) / previous_count_1h

    @Column(name = "velocity_24h")
    private Double velocity24h;

    @Column(name = "acceleration")
    private Double acceleration; // change in velocity (is it speeding up?)

    // ================= ENGAGEMENT METRICS =================

    @Builder.Default
    @Column(name = "total_views_24h", nullable = false)
    private Long totalViews24h = 0L;

    @Builder.Default
    @Column(name = "total_reactions_24h", nullable = false)
    private Long totalReactions24h = 0L;

    @Builder.Default
    @Column(name = "unique_authors_24h", nullable = false)
    private Long uniqueAuthors24h = 0L;

    // ================= TRENDING CALCULATION =================

    @Builder.Default
    @Column(name = "trending_score", nullable = false)
    private Double trendingScore = 0.0;

    @Column(name = "normalized_score")
    private Double normalizedScore; // 0-100 scale for display

    @Column(name = "peak_score")
    private Double peakScore;

    @Column(name = "peak_at")
    private LocalDateTime peakAt;

    // ================= STATUS =================

    @Builder.Default
    @Column(name = "is_trending", nullable = false)
    private Boolean isTrending = false;

    @Column(name = "trend_rank")
    private Integer trendRank; // 1 = top trending

    @Column(name = "previous_rank")
    private Integer previousRank;

    @Column(name = "rank_change")
    private Integer rankChange; // positive = moving up

    @Column(name = "trending_since")
    private LocalDateTime trendingSince;

    @Column(name = "trending_duration_hours")
    private Integer trendingDurationHours;

    // ================= METADATA =================

    @Builder.Default
    @Column(name = "last_calculated_at", nullable = false)
    private LocalDateTime lastCalculatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "calculation_version", nullable = false)
    private Integer calculationVersion = 1; // for algorithm versioning

    // ================= HELPER METHODS =================

    public void updateRank(Integer newRank) {
        this.previousRank = this.trendRank;
        this.trendRank = newRank;
        this.rankChange = (previousRank != null && newRank != null)
                ? previousRank - newRank
                : null;
    }

    public void markAsTrending() {
        if (!Boolean.TRUE.equals(this.isTrending)) {
            this.isTrending = true;
            this.trendingSince = LocalDateTime.now();
        }
    }

    public void markAsNotTrending() {
        this.isTrending = false;
        this.trendRank = null;
        this.trendingSince = null;
        this.trendingDurationHours = null;
    }

    public void updatePeakIfHigher(Double currentScore) {
        if (this.peakScore == null || currentScore > this.peakScore) {
            this.peakScore = currentScore;
            this.peakAt = LocalDateTime.now();
        }
    }
}
