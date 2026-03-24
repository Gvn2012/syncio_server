package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.exceptions.IllegalStateException;
import io.github.gvn2012.post_service.entities.enums.EventStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "post_events", indexes = {
        @Index(name = "ix_post_events_post_id", columnList = "post_id"),
        @Index(name = "ix_post_events_start_time", columnList = "start_time"),
        @Index(name = "ix_post_events_status", columnList = "event_status")
})
public class PostEvent extends AuditableEntity {

    @Id
    @Column(name = "post_id", columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID postId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "title")
    private String title;

    @Column(name = "location")
    private String location;

    @Column(name = "latitude")
    private Double latitude = null;

    @Column(name = "longitude")
    private Double longitude = null;

    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "max_participants")
    @Min(1)
    private Integer maxParticipants;

    @Column(name = "allow_guests", nullable = false)
    private Boolean allowGuests = false;

    @Column(name = "require_approval", nullable = false)
    private Boolean requireApproval = true;

    @Column(name = "rsvp_deadline")
    private LocalDateTime rsvpDeadline;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "accepted_count")
    private Integer acceptedCount = 0;

    @Column(name = "declined_count")
    private Integer declinedCount = 0;

    @Column(name = "tentative_count")
    private Integer tentativeCount = 0;

    @Column(name = "timezone", nullable = false)
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false)
    private EventStatus status = EventStatus.SCHEDULED;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostEventParticipant> participants = new LinkedHashSet<>();

    @PreUpdate
    private void validateEvent() {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalStateException("End time must be after start time");
        }

        if (rsvpDeadline != null && startTime != null && rsvpDeadline.isAfter(startTime)) {
            throw new IllegalStateException("RSVP deadline must be before start time");
        }
    }

}
