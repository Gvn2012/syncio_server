package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "tags", indexes = {
        @Index(name = "ix_tags_name", columnList = "name"),
        @Index(name = "ix_tags_post_count", columnList = "post_count DESC")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_tags_name", columnNames = "name")
})
public class Tag extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Size(max = 128)
    @ToString.Include
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 128)
    @Column(name = "display_name", length = 128)
    private String displayName;

    @Column(name = "post_count", nullable = false)
    private Long postCount = 0L;

    @Column(name = "follower_count", nullable = false)
    private Long followerCount = 0L;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @ToString.Exclude
    @OneToOne(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private TagTrending trending;
}
