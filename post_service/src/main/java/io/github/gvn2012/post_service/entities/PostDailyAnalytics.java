package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.composite_keys.PostDailyAnalyticsId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_daily_analytics", indexes = {
        @Index(name = "ix_post_analytics_post_date", columnList = "post_id, analytics_date"),
        @Index(name = "ix_post_analytics_date", columnList = "analytics_date")
})
@IdClass(PostDailyAnalyticsId.class)
public class PostDailyAnalytics {

    @Id
    @Column(name = "post_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID postId;

    @Id
    @Column(name = "analytics_date", nullable = false)
    private LocalDate analyticsDate;

    @Column(name = "view_count", nullable = false)
    private BigInteger viewCount = BigInteger.ZERO;

    @Column(name = "unique_view_count", nullable = false)
    private BigInteger uniqueViewCount = BigInteger.ZERO;

    @Column(name = "reaction_count", nullable = false)
    private Integer reactionCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount = 0;

    @Column(name = "click_count", nullable = false)
    private Integer clickCount = 0;

    @Column(name = "total_dwell_seconds", nullable = false)
    private Long totalDwellSeconds = 0L;

    @Column(name = "avg_dwell_seconds")
    private Double avgDwellSeconds;

    @Column(name = "reach_count", nullable = false)
    private BigInteger reachCount = BigInteger.ZERO;

    @Column(name = "impression_count", nullable = false)
    private BigInteger impressionCount = BigInteger.ZERO;

    // Engagement rate = (reactions + comments + shares) / impressions
    @Column(name = "engagement_rate")
    private Double engagementRate;

    // Traffic sources breakdown
    @Column(name = "source_breakdown", columnDefinition = "json")
    private String sourceBreakdown; // {"feed": 1000, "profile": 200, "search": 50, "direct": 30}

    // Device breakdown
    @Column(name = "device_breakdown", columnDefinition = "json")
    private String deviceBreakdown; // {"mobile": 800, "desktop": 400, "tablet": 80}
}
