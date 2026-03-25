package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.ReportReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "moderation_queue_reports", indexes = {
        @Index(name = "ix_mod_reports_queue", columnList = "moderation_queue_id"),
        @Index(name = "ix_mod_reports_reporter", columnList = "reporter_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_mod_report_user", columnNames = {"moderation_queue_id", "reporter_id"})
})
public class ModerationQueueReport extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moderation_queue_id", nullable = false)
    private ModerationQueue moderationQueue;

    @NotNull
    @Column(name = "reporter_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID reporterId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "description", columnDefinition = "VARCHAR(2048)")
    private String description;

    @Column(name = "evidence_urls", columnDefinition = "json")
    private String evidenceUrls;
}
