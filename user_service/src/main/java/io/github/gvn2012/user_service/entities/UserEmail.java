package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_emails", indexes = {
                @Index(name = "ix_user_emails_user", columnList = "user_id"),
                @Index(name = "ix_user_emails_user_primary", columnList = "user_id, is_primary"),
                @Index(name = "ix_user_emails_user_status", columnList = "user_id, status")
})
public class UserEmail extends AuditableEntity {

        @Id
        @UuidGenerator(style = UuidGenerator.Style.TIME)
        @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false)
        @NotNull
        private User user;

        @Column(name = "email", nullable = false)
        @NotBlank
        @Email
        @Size(min = 5, max = 255)
        private String email;

        @Column(name = "is_verified", nullable = false)
        private Boolean verified = false;

        @Column(name = "is_primary", nullable = false)
        private Boolean primary = false;

        @Column(name = "verified_at")
        private LocalDateTime verifiedAt;

        @Column(name = "verification_token_hash", length = 512)
        private String verificationTokenHash;

        @Column(name = "verification_token_hash_expires_at")
        private LocalDateTime verificationTokenHashExpiresAt;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = 32)
        private EmailStatus status = EmailStatus.UNVERIFIED;
}