package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.PreferenceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_preferences",
        indexes = {
                @Index(name = "ix_user_preferences_user", columnList = "user_id"),
                @Index(name = "ix_user_preferences_user_category", columnList = "user_id, category")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_preferences_user_key",
                        columnNames = {"user_id", "preference_key"}
                )
        }
)
public class UserPreference extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private PreferenceCategory category;

    @Column(name = "preference_key", nullable = false, length = 128)
    @NotBlank
    @Size(max = 128)
    private String preferenceKey;

    @Column(name = "preference_value", columnDefinition = "TEXT")
    private String preferenceValue;
}
