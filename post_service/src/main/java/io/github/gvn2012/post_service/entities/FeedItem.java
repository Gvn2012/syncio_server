package io.github.gvn2012.post_service.entities;

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
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "feed_items", indexes = {
        @Index(name = "ix_feed_items_recipient", columnList = "recipient_id"),
        @Index(name = "ix_feed_items_recipient_score_created", columnList = "recipient_id, weight_score DESC, created_at DESC")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_feed_item_post_recipient", columnNames = {"source_post_id", "recipient_id"})
})
public class FeedItem {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "recipient_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID recipientId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_post_id", nullable = false, updatable = false)
    private Post sourcePost;

    @NotNull
    @Column(name = "weight_score", nullable = false)
    private Double weightScore;

    @Column(name = "reason_code", columnDefinition = "VARCHAR(64)")
    private String reasonCode;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "org_id", nullable = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID orgId;
}
