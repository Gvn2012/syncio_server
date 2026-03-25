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
@Table(name = "post_collection_items", indexes = {
        @Index(name = "ix_collection_items_collection", columnList = "collection_id"),
        @Index(name = "ix_collection_items_post", columnList = "post_id"),
        @Index(name = "ix_collection_items_added", columnList = "added_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_collection_post", columnNames = {"collection_id", "post_id"})
})
public class PostCollectionItem extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private PostCollection collection;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @NotNull
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "note", columnDefinition = "VARCHAR(512)")
    private String note;

    @Column(name = "display_order")
    private Integer displayOrder;
}
