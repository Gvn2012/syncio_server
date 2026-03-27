package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.PhoneStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_phones",
        indexes = {
                @Index(name = "ix_user_phones_user", columnList = "user_id"),
                @Index(name = "ix_user_phones_user_primary", columnList = "user_id, is_primary"),
                @Index(name = "ix_user_phones_user_verified", columnList = "user_id, is_verified")
        }
)
public class UserPhone extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "phone_number", nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[0-9+\\-() ]+$")
    private String phoneNumber;

    @Column(name = "country_code", nullable = false)
    @Pattern(regexp = "^(\\+?\\d{1,3}|\\d{1,4})$")
    private String countryCode = "+84";

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "status", nullable = false)
    private PhoneStatus status = PhoneStatus.ACTIVE;

    @Column(name = "verification_code_hash", length = 256)
    private String verificationCodeHash;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}