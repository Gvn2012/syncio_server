package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "content_rankings", indexes = {
        @Index(name = "ix_content_rankings_score", columnList = "engagement_score DESC"),
        @Index(name = "ix_content_rankings_org", columnList = "org_id")
})
public class ContentRanking {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "post_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID postId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @NotNull
    @Column(name = "engagement_score", nullable = false)
    private Double engagementScore = 0.0;

    @NotNull
    @Column(name = "freshness_score", nullable = false)
    private Double freshnessScore = 0.0;

    @NotNull
    @Column(name = "business_boost", nullable = false)
    private Double businessBoost = 0.0;

    @NotNull
    @Column(name = "total_score", nullable = false)
    private Double totalScore = 0.0;

    @NotNull
    @Column(name = "last_computed_at", nullable = false)
    private LocalDateTime lastComputedAt = LocalDateTime.now();

    @NotNull
    @Column(name = "org_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID orgId;
}
