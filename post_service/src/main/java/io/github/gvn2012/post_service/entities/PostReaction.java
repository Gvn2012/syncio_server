package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.ReactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "post_reactions", indexes = {
                @Index(name = "ix_post_reactions_post", columnList = "post_id"),
                @Index(name = "ix_post_reactions_user", columnList = "user_id"),
                @Index(name = "ix_post_reactions_type", columnList = "reaction_type")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_user_reaction", columnNames = { "post_id", "user_id",
                                "reaction_type" })
})
public class PostReaction extends AuditableEntity {

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
        @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID userId;

        @NotNull
        @Enumerated(EnumType.STRING)
        @Column(name = "reaction_type", nullable = false, length = 32)
        private ReactionType reactionType;
}
