package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.AttachmentType;
import io.github.gvn2012.post_service.entities.enums.AttachmentUploadStatus;
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
@Table(
        name = "post_media_attachments",
        indexes = {
                @Index(name = "ix_post_media_post_id", columnList = "post_id"),
                @Index(name = "ix_post_media_upload_status", columnList = "upload_status"),
                @Index(name = "ix_post_media_type", columnList = "type")
        }
)
public class PostMediaAttachment extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull
    private Post post;

    @ToString.Include
    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "position", nullable = false)
    private Byte position = 0;

    @Column(name = "caption", columnDefinition = "VARCHAR(1024)")
    private String caption;

    @Column(name = "alt_text", columnDefinition = "VARCHAR(1024)")
    private String altText;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private AttachmentUploadStatus uploadStatus = AttachmentUploadStatus.PENDING;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AttachmentType type = AttachmentType.IMAGE;

    @Column(name = "mime_type", nullable = false, columnDefinition = "VARCHAR(24)")
    private String mimeType = "image/png";

    @Column(name = "width")
    private Integer width;

    @Column(name = "length")
    private Integer length;

    @Column(name = "duration")
    private Double duration;
}