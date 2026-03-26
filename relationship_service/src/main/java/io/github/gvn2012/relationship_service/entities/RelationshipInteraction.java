package io.github.gvn2012.relationship_service.entities;

import io.github.gvn2012.relationship_service.entities.enums.InteractionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "relationship_interactions", indexes = {
        @Index(name = "ix_interaction_users", columnList = "source_user_id, target_user_id"),
        @Index(name = "ix_interaction_time", columnList = "interacted_at"),
        @Index(name = "ix_interaction_type", columnList = "interaction_type")
})
public class RelationshipInteraction {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "source_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID sourceUserId;

    @NotNull
    @Column(name = "target_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID targetUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Column(name = "content_id", columnDefinition = "BINARY(16)")
    private UUID contentId; // post, comment, message id

    @Column(name = "weight", nullable = false)
    private Double weight = 1.0;

    @NotNull
    @Column(name = "interacted_at", nullable = false)
    private LocalDateTime interactedAt = LocalDateTime.now();
}
