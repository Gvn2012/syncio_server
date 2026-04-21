package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.ModerationAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "moderation_queue_audit", indexes = {
        @Index(name = "ix_mod_audit_queue", columnList = "moderation_queue_id"),
        @Index(name = "ix_mod_audit_actor", columnList = "actor_id")
})
public class ModerationQueueAudit extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moderation_queue_id", nullable = false)
    private ModerationQueue moderationQueue;

    @NotNull
    @Column(name = "actor_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID actorId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ModerationAction action;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "note", columnDefinition = "VARCHAR(1024)")
    private String note;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
