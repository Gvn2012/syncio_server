package io.github.gvn2012.post_service.entities;

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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "post_reactions", indexes = {
        @Index(name = "ix_post_reactions_post", columnList = "post_id"),
        @Index(name = "ix_post_reactions_user", columnList = "user_id"),
        @Index(name = "ix_post_reactions_type", columnList = "reaction_type_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_user_reaction", columnNames = {"post_id", "user_id", "reaction_type_id"})
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reaction_type_id", nullable = false)
    private ReactionType reactionType;
}
