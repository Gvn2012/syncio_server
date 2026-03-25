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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "post_comment_mentions", indexes = {
        @Index(name = "ix_comment_mentions_comment", columnList = "comment_id"),
        @Index(name = "ix_comment_mentions_user", columnList = "mentioned_user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_mention_user", columnNames = {"comment_id", "mentioned_user_id"})
})
public class PostCommentMention extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false, updatable = false)
    private PostComment comment;

    @NotNull
    @Column(name = "mentioned_user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID mentionedUserId;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;
}
