package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.EventParticipantStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "post_event_participants",
        indexes = {
                @Index(name = "ix_event_participant_event", columnList = "event_id"),
                @Index(name = "ix_event_participant_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_user",
                        columnNames = {"event_id", "user_id"}
                )
        }
)
public class PostEventParticipant extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private PostEvent event;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventParticipantStatus status = EventParticipantStatus.NOT_RESPONDED;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}