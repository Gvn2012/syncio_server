package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import io.github.gvn2012.post_service.exceptions.IllegalStateException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "posts", indexes = {
                @Index(name = "ix_posts_visibility", columnList = "visibility"),
                @Index(name = "ix_posts_status", columnList = "status"),
                @Index(name = "ix_posts_parent_post", columnList = "parent_post_id")
})
public class Post extends AuditableEntity {

        @Id
        @EqualsAndHashCode.Include
        @ToString.Include
        @UuidGenerator(style = UuidGenerator.Style.TIME)
        @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID id;

        @Enumerated(EnumType.STRING)
        @Column(name = "post_type", nullable = false)
        private PostCategory postCategory = PostCategory.NORMAL;

        @Column(name = "org_id", nullable = true, updatable = false, columnDefinition = "BINARY(16)")
        private UUID orgId;

        @Column(name = "author_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID authorId;

        @ToString.Include
        @Column(name = "content", columnDefinition = "TEXT")
        private String content;

        @Column(name = "content_html", columnDefinition = "MEDIUMTEXT")
        private String contentHtml;

        @Column(name = "excerpt", columnDefinition = "VARCHAR(1024)")
        private String excerpt;

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

        @Column(name = "published_at", nullable = false, updatable = false)
        private LocalDateTime publishedAt = LocalDateTime.now();

        @Column(name = "archived_at")
        private LocalDateTime archivedAt;

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
        private String nativeLinkPreview;

        @Column(name = "metadata", columnDefinition = "json")
        private String metadata;

        @Column(name = "is_shared", nullable = false)
        private Boolean isShared = false;

        @Column(name = "is_pinned", nullable = false)
        private Boolean isPinned = false;

        // ================= RELATIONSHIPS =================

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_post_id")
        private Post parentPost;

        @OneToMany(mappedBy = "parentPost")
        private Set<Post> sharedPosts = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMediaAttachment> attachments = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMention> mentions = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PostTag> postTags = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("version DESC")
        private Set<PostEditHistory> editHistory = new LinkedHashSet<>();

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostEvent event;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostPoll poll;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostTask task;

        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostAnnouncement announcement;

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @OrderBy("createdAt DESC")
        private Set<PostComment> comments = new LinkedHashSet<>();

        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PostReaction> reactions = new LinkedHashSet<>();

}