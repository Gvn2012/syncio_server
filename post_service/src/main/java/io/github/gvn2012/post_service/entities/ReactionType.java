package io.github.gvn2012.post_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "reaction_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reaction_type_code", columnNames = "code")
})
public class ReactionType {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Short id;

    @NotBlank
    @Size(max = 32)
    @ToString.Include
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @NotBlank
    @Size(max = 64)
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Size(max = 16)
    @Column(name = "emoji", length = 16)
    private String emoji;

    @Size(max = 255)
    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
