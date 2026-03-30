package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
        name = "office_locations",
        indexes = {
                @Index(name = "ix_office_locations_org", columnList = "organization_id"),
                @Index(name = "ix_office_locations_status", columnList = "status"),
                @Index(name = "ix_office_locations_hq", columnList = "organization_id, is_headquarters")
        }
)
public class OfficeLocation extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "address_line_1", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String addressLine1;

    @Column(name = "address_line_2")
    @Size(max = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 128)
    @NotBlank
    private String city;

    @Column(name = "state", length = 128)
    private String state;

    @Column(name = "country", nullable = false, length = 64)
    @NotBlank
    private String country;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "phone_number", length = 64)
    private String phoneNumber;

    @Column(name = "email")
    @Email
    @Size(max = 255)
    private String email;

    @Column(name = "is_headquarters", nullable = false)
    private Boolean headquarters = false;

    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OfficeStatus status = OfficeStatus.OPEN;
}
