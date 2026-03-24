package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
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
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "tags",
        indexes = {
                @Index(name = "ix_tag_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tag_name", columnNames = "name")
        }
)
public class Tag extends AuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ToString.Include
    @Column(name = "name", nullable = false)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "tag")
    private Set<PostTag> postTags = new LinkedHashSet<>();
}