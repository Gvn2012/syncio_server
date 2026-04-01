package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.ModerationAction;
import io.github.gvn2012.post_service.entities.enums.ModerationContentType;
import io.github.gvn2012.post_service.entities.enums.ModerationPriority;
import io.github.gvn2012.post_service.entities.enums.ModerationQueueStatus;
import jakarta.persistence.*;
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
@Table(name = "moderation_queue", indexes = {
        @Index(name = "ix_moderation_queue_status", columnList = "status"),
        @Index(name = "ix_moderation_queue_priority", columnList = "priority, created_at"),
        @Index(name = "ix_moderation_queue_content", columnList = "content_type, content_id"),
        @Index(name = "ix_moderation_queue_assignee", columnList = "assigned_to")
})
public class ModerationQueue extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ModerationContentType contentType;

    @NotNull
    @Column(name = "content_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID contentId;

    @NotNull
    @Column(name = "author_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID authorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ModerationQueueStatus status = ModerationQueueStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ModerationPriority priority = ModerationPriority.NORMAL;

    @Column(name = "auto_flag_reason", columnDefinition = "VARCHAR(512)")
    private String autoFlagReason;

    @Column(name = "auto_flag_confidence")
    private Double autoFlagConfidence;

    @Column(name = "auto_flag_categories", columnDefinition = "json")
    private String autoFlagCategories; // ["spam", "hate_speech", "violence"]

    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot; // preserved at flag time

    @Column(name = "assigned_to", columnDefinition = "BINARY(16)")
    private UUID assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", columnDefinition = "BINARY(16)")
    private UUID resolvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_action")
    private ModerationAction resolutionAction;

    @Column(name = "resolution_note", columnDefinition = "VARCHAR(1024)")
    private String resolutionNote;

    @Column(name = "appeal_count", nullable = false)
    private Integer appealCount = 0;

    @ToString.Exclude
    @OneToMany(mappedBy = "moderationQueue", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt DESC")
    private Set<ModerationQueueReport> reports = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "moderationQueue", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC")
    private Set<ModerationQueueAudit> auditLog = new LinkedHashSet<>();
}
