package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.CommentModerationStatus;
import io.github.gvn2012.post_service.entities.enums.CommentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
@Table(name = "post_comments", indexes = {
        @Index(name = "ix_post_comments_post", columnList = "post_id"),
        @Index(name = "ix_post_comments_user", columnList = "user_id"),
        @Index(name = "ix_post_comments_parent", columnList = "parent_comment_id"),
        @Index(name = "ix_post_comments_status", columnList = "status"),
        @Index(name = "ix_post_comments_created_at", columnList = "created_at")
})
public class PostComment extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @NotNull
    @ToString.Include
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @ToString.Include
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.VISIBLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private CommentModerationStatus moderationStatus = CommentModerationStatus.NONE;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "edit_count", nullable = false)
    @Min(0)
    private Integer editCount = 0;

    @Column(name = "reaction_count", nullable = false)
    @Min(0)
    private Integer reactionCount = 0;

    @Column(name = "reply_count", nullable = false)
    @Min(0)
    private Integer replyCount = 0;

    @Column(name = "depth", nullable = false)
    @Min(0)
    private Integer depth = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_author_reply", nullable = false)
    private Boolean isAuthorReply = false;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    // ================= RELATIONSHIPS =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @ToString.Exclude
    @OneToMany(mappedBy = "parentComment", fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private Set<PostComment> replies = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostCommentReaction> reactions = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostCommentMention> mentions = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostCommentEditHistory> editHistory = new LinkedHashSet<>();
}
