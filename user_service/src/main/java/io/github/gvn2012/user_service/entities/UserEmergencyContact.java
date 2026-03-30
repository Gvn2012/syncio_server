package io.github.gvn2012.user_service.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_emergency_contacts",
        indexes = {
                @Index(name = "ix_emergency_contacts_user", columnList = "user_id"),
                @Index(name = "ix_emergency_contacts_user_primary", columnList = "user_id, is_primary")
        }
)
public class UserEmergencyContact extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "contact_name", nullable = false)
    @NotBlank
    @Size(max = 128)
    private String contactName;

    @Column(name = "relationship", nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    private String relationship;

    @Column(name = "phone_number", nullable = false, length = 64)
    @NotBlank
    private String phoneNumber;

    @Column(name = "email")
    @Email
    @Size(max = 255)
    private String email;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;

    @Column(name = "priority", nullable = false)
    @Min(1)
    @Max(10)
    private Integer priority = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private io.github.gvn2012.user_service.entities.enums.EmergencyContactStatus status = io.github.gvn2012.user_service.entities.enums.EmergencyContactStatus.ACTIVE;
}
