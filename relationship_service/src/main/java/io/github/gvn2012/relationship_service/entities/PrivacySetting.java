package io.github.gvn2012.relationship_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "privacy_settings", indexes = {
        @Index(name = "ix_privacy_user_id", columnList = "user_id")
})
public class PrivacySetting extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @NotNull
    @Column(name = "setting_key", nullable = false, length = 64)
    private String settingKey;

    @NotNull
    @Column(name = "setting_value", nullable = false, length = 64)
    private String settingValue;
}
