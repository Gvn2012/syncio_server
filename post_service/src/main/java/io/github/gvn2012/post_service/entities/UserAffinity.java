package io.github.gvn2012.post_service.entities;

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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "user_affinity", indexes = {
        @Index(name = "ix_user_affinity_user_author", columnList = "user_id, author_id", unique = true),
        @Index(name = "ix_user_affinity_score", columnList = "user_id, affinity_score DESC")
})
public class UserAffinity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @NotNull
    @Column(name = "author_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID authorId;

    @NotNull
    @Column(name = "affinity_score", nullable = false)
    private Double affinityScore = 0.0;

    @NotNull
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @NotNull
    @Column(name = "org_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID orgId;
}
