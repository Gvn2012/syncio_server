package io.github.gvn2012.user_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_profile_pictures",
        indexes = {
                @Index(name = "ix_profile_pictures_profile", columnList = "user_profile_id"),
                @Index(name = "ix_profile_pictures_profile_primary", columnList = "user_profile_id, is_primary"),
                @Index(name = "ix_profile_pictures_profile_deleted", columnList = "user_profile_id, is_deleted")
        }
)
public class UserProfilePicture extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "file_size")
    @PositiveOrZero
    private Long fileSize;

    @Column(name = "height")
    @Positive
    private Integer height;

    @Column(name = "width")
    @Positive
    private Integer width;

    @Column(name = "url", nullable = false, length = 1024)
    @NotBlank
    private String url;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;
}