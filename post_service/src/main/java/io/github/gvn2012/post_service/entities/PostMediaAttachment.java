package io.github.gvn2012.post_service.entities;


import io.github.gvn2012.post_service.entities.enums.AttachmentType;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Data
@Table(
        name = "post_media_attachments",
        indexes = {
                @Index(name = "ix_posts_visibility", columnList = "visibility"),
                @Index(name = "ix_posts_status", columnList = "status")
        }
)
public class PostMediaAttachment extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull
    private Post post;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "position", nullable = false)
    private Byte position = 0;

    @Column(name = "caption", columnDefinition = "VARCHAR(1024)")
    private String caption = null;

    @Column(name = "alt_text", columnDefinition = "VARCHAR(1024)")
    private String altText = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private AttachmentUploadStatus upload_status = AttachmentUploadStatus.PENDING;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AttachmentType type = AttachmentType.IMAGE;

    @Column(name = "mime_type", nullable = false, columnDefinition = "VARCHAR(24)")
    private String mimeType = "png";

    @Column(name = "width")
    private Integer width = null;

    @Column(name = "length")
    private Integer length = null;

    @Column(name = "duration")
    private Double duration = null;

}
