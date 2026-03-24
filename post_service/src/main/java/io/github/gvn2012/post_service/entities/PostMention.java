package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.MentionStatus;
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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "post_mentions",
        indexes = {
                @Index(name = "ix_post_mention_user_id", columnList = "user_id"),
                @Index(name = "ix_post_mention_status", columnList = "status"),
                @Index(name = "ix_post_mention_post_id", columnList = "post_id")
        },
        uniqueConstraints = {
                @
                        UniqueConstraint(name = "uk_post_user", columnNames = {"post_id", "user_id"})
        }
)
public class PostMention extends AuditableEntity {

        @Id
        @ToString.Include
        @EqualsAndHashCode.Include
        @UuidGenerator(style = UuidGenerator.Style.TIME)
        @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID id;

        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        @NotNull
        private Post post;

        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private MentionStatus status = MentionStatus.ACTIVE;
}