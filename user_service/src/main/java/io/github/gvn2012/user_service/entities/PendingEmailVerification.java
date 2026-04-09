package io.github.gvn2012.user_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pending_email_verifications", indexes = {
        @Index(name = "ix_pending_email_verifications_email", columnList = "email"),
        @Index(name = "ix_pending_email_verifications_verified", columnList = "is_verified"),
        @Index(name = "ix_pending_email_verifications_consumed", columnList = "consumed_at")
})
public class PendingEmailVerification extends AuditableEntity {

    @jakarta.persistence.Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "email", nullable = false, length = 255)
    @NotBlank
    @Email
    @Size(min = 5, max = 255)
    private String email;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_code_hash", length = 256)
    private String verificationCodeHash;

    @Column(name = "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;

    @Column(name = "resend_available_at")
    private LocalDateTime resendAvailableAt;

    @Column(name = "registration_expires_at")
    private LocalDateTime registrationExpiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;
}
