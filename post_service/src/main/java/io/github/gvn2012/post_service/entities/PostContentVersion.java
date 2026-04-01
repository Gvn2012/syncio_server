package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.DiffAlgorithm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "post_content_versions", indexes = {
        @Index(name = "ix_content_version_post", columnList = "post_id"),
        @Index(name = "ix_content_version_number", columnList = "post_id, version_number")
})
public class PostContentVersion extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "content_diff", columnDefinition = "MEDIUMBLOB")
    private byte[] contentDiff;

    @Column(name = "content_snapshot", columnDefinition = "MEDIUMTEXT")
    private String contentSnapshot;

    @Column(name = "is_snapshot", nullable = false)
    private Boolean isSnapshot = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "diff_algorithm", nullable = false)
    private DiffAlgorithm diffAlgorithm = DiffAlgorithm.MYERS;

    @Column(name = "compressed_size")
    private Integer compressedSize;

    @Column(name = "original_size")
    private Integer originalSize;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Column(name = "created_by", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID createdBy;
}
