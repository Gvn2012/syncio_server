package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostType;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Data
@Table(name = "posts", indexes = {
                @Index(name = "ix_posts_visibility", columnList = "visibility"),
                @Index(name = "ix_posts_status", columnList = "status")
})
public class Post extends AuditableEntity {

        @Id
        @UuidGenerator(style = UuidGenerator.Style.TIME)
        @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID id;

        @Enumerated(EnumType.STRING)
        @Column(name = "post_type", nullable = false)
        private PostType postType = PostType.MIXED;

        @Column(name = "content", columnDefinition = "TEXT")
        private String content = null;

        @Column(name = "content_html", columnDefinition = "MEDIUMTEXT")
        private String contentHtml = null;

        @Column(name = "excerpt", columnDefinition = "VARCHAR(1024)")
        private String excerpt = null;

        @Column(name = "language", columnDefinition = "VARCHAR(16)", nullable = false)
        private String language = "en";

        @Enumerated(EnumType.STRING)
        @Column(name = "visibility", nullable = false)
        private PostVisibility visibility = PostVisibility.PUBLIC;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private PostStatus status = PostStatus.PUBLISHED;

        @Enumerated(EnumType.STRING)
        @Column(name = "moderation_status", nullable = false)
        private PostModerationStatus moderationStatus = PostModerationStatus.NONE;

        @Column(name = "is_scheduled", nullable = false)
        private Boolean isScheduled = false;

        @Column(name = "scheduled_for")
        private LocalDateTime scheduledFor = null;

        @Column(name = "published_at", nullable = false, updatable = false)
        private LocalDateTime publishedAt = LocalDateTime.now();

        @Column(name = "archived_at")
        private LocalDateTime archivedAt = null;

        @Column(name = "edit_count", nullable = false)
        @Min(0)
        private Integer editCount = 0;

        @Column(name = "comment_count", nullable = false)
        @Min(0)
        private Integer commentCount = 0;

        @Column(name = "reaction_count", nullable = false)
        @Min(0)
        private Integer reactionCount = 0;

        @Column(name = "share_count", nullable = false)
        @Min(0)
        private Integer shareCount = 0;

        @Column(name = "view_count", nullable = false)
        @Min(0)
        private BigInteger viewCount = BigInteger.ZERO;

        @Column(name = "avg_dwell_seconds")
        @Min(0)
        private Double avgDwellSeconds = 0d;

        @Column(name = "native_link_preview", columnDefinition = "json")
        private String nativeLinkPreview = null;

        @Column(name = "metadata", columnDefinition = "json")
        private String metadata = null;

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMediaAttachment> attachments = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMention> mentions = new LinkedHashSet<>();

}
