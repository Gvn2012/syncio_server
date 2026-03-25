package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.composite_keys.PostTagId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "post_tags",
        indexes = {
                @Index(name = "ix_post_tag_post_id", columnList = "post_id"),
                @Index(name = "ix_post_tag_tag_id", columnList = "tag_id")
        }
)
public class PostTag extends AuditableEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private PostTagId id = new PostTagId();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
