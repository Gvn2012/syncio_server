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
@Table(name = "post_comment_edit_history", indexes = {
        @Index(name = "ix_comment_edit_history_comment", columnList = "comment_id"),
        @Index(name = "ix_comment_edit_history_edited_at", columnList = "edited_at")
})
public class PostCommentEditHistory extends AuditableEntity {

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

    @ToString.Include
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "previous_content", columnDefinition = "TEXT")
    private String previousContent;

    @Column(name = "previous_content_html", columnDefinition = "TEXT")
    private String previousContentHtml;

    @NotNull
    @Column(name = "edited_at", nullable = false, updatable = false)
    private LocalDateTime editedAt = LocalDateTime.now();
}
