package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import io.github.gvn2012.post_service.entities.enums.MentionStatus;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "post_mentions",
        indexes = {
                @Index(name = "ix_post_mention_user_id", columnList = "user_id"),
                @Index(name = "ix_post_mention_status", columnList = "status"),
                @Index(name = "ix_post_mention_post_id", columnList = "post_id")
        }
)
public class PostMention extends AuditableEntity {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
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
