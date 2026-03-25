package io.github.gvn2012.post_service.entities;

import io.github.gvn2012.post_service.entities.enums.CollectionVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_collections", indexes = {
        @Index(name = "ix_post_collections_user", columnList = "user_id"),
        @Index(name = "ix_post_collections_visibility", columnList = "visibility")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_collection_user_name", columnNames = {"user_id", "name"})
})
public class PostCollection extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @NotBlank
    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 512)
    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private CollectionVisibility visibility = CollectionVisibility.PRIVATE;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false; // "Saved Posts" default collection

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @ToString.Exclude
    @OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("addedAt DESC")
    private Set<PostCollectionItem> items = new LinkedHashSet<>();
}
