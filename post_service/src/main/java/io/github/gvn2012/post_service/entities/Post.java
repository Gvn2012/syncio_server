package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.PostModerationStatus;
import io.github.gvn2012.post_service.entities.enums.PostStatus;
import io.github.gvn2012.post_service.entities.enums.PostCategory;
import io.github.gvn2012.post_service.entities.enums.PostVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import io.github.gvn2012.post_service.exceptions.IllegalStateException;

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

        @Column(name = "org_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
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

        // ================= RELATIONSHIPS =================

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_post_id")
        private Post parentPost;

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @OneToMany(mappedBy = "parentPost")
        private Set<Post> sharedPosts = new LinkedHashSet<>();

        @ToString.Exclude
        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMediaAttachment> attachments = new LinkedHashSet<>();

        @ToString.Exclude
        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Set<PostMention> mentions = new LinkedHashSet<>();

        @ToString.Exclude
        @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PostTag> postTags = new LinkedHashSet<>();

        @ToString.Exclude
        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("version DESC")
        private Set<PostEditHistory> editHistory = new LinkedHashSet<>();

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostEvent event;

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostPoll poll;

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostTask task;

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
        private PostAnnouncement announcement;

        @ToString.Exclude
        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @OrderBy("createdAt DESC")
        private Set<PostComment> comments = new LinkedHashSet<>();

        @ToString.Exclude
        @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<PostReaction> reactions = new LinkedHashSet<>();

        // ================= VALIDATION =================

        @PreUpdate
        private void validate() {

                // ===== Shared post validation =====
                if (Boolean.TRUE.equals(isShared) && parentPost == null) {
                        throw new IllegalStateException("Shared post must have parentPost");
                }

                if (Boolean.FALSE.equals(isShared) && parentPost != null) {
                        throw new IllegalStateException("Non-shared post cannot have parentPost");
                }

                // ===== Extension validation =====
                switch (postCategory) {
                        case NORMAL -> {
                                if (event != null || poll != null || task != null || announcement != null) {
                                        throw new IllegalStateException(
                                                        "Normal post cannot have PostEvent, PostPoll, PostTask, or PostAnnouncement");
                                }
                        }
                        case EVENT -> {
                                if (event == null) {
                                        throw new IllegalStateException("Event post must have PostEvent");
                                }
                        }
                        case POLL -> {
                                if (poll == null) {
                                        throw new IllegalStateException("Poll must have PostPoll");
                                }
                        }
                        case TASK -> {
                                if (task == null) {
                                        throw new IllegalStateException("Task post must have PostTask");
                                }
                        }
                        case ANNOUNCEMENT -> {
                                if (announcement == null) {
                                        throw new IllegalStateException("Announcement post must have PostAnnouncement");
                                }
                        }
                }
        }
}