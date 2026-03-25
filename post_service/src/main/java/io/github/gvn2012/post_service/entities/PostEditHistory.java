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
@Table(name = "post_edit_history", indexes = {
        @Index(name = "ix_post_edit_history_post", columnList = "post_id"),
        @Index(name = "ix_post_edit_history_edited_at", columnList = "edited_at")
})
public class PostEditHistory extends AuditableEntity {

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

    @ToString.Include
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "previous_content", columnDefinition = "TEXT")
    private String previousContent;

    @Column(name = "previous_content_html", columnDefinition = "MEDIUMTEXT")
    private String previousContentHtml;

    @Column(name = "previous_excerpt", columnDefinition = "VARCHAR(1024)")
    private String previousExcerpt;

    @Column(name = "previous_metadata", columnDefinition = "json")
    private String previousMetadata;

    @Column(name = "edit_reason", columnDefinition = "VARCHAR(512)")
    private String editReason;

    @NotNull
    @Column(name = "edited_at", nullable = false, updatable = false)
    private LocalDateTime editedAt = LocalDateTime.now();

    @NotNull
    @Column(name = "edited_by", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID editedBy;
}
