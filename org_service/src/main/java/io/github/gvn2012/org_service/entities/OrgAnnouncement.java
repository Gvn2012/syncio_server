package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.AnnouncementPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "org_announcements",
        indexes = {
                @Index(name = "ix_org_announcements_org", columnList = "organization_id"),
                @Index(name = "ix_org_announcements_dept", columnList = "department_id"),
                @Index(name = "ix_org_announcements_author", columnList = "author_id"),
                @Index(name = "ix_org_announcements_priority", columnList = "priority"),
                @Index(name = "ix_org_announcements_pinned", columnList = "organization_id, is_pinned")
        }
)
public class OrgAnnouncement extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** Nullable — null means org-wide, non-null scopes to a department */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "title", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String content;

    /** References User.id in user_service */
    @Column(name = "author_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private AnnouncementPriority priority = AnnouncementPriority.NORMAL;

    @Column(name = "is_pinned", nullable = false)
    private Boolean pinned = false;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
