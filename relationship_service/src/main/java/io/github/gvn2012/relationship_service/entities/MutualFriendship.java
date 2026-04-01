package io.github.gvn2012.relationship_service.entities;

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
@Builder
@Table(name = "mutual_friendships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mutual_u1_u2", columnNames = {"user1_id", "user2_id"})
})
public class MutualFriendship extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user1_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID user1Id;

    @NotNull
    @Column(name = "user2_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID user2Id;

    @Builder.Default
    @Column(name = "mutual_count", nullable = false)
    private Integer mutualCount = 0;
}
