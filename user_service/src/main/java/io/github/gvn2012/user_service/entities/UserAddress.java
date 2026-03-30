package io.github.gvn2012.user_service.entities;

import io.github.gvn2012.user_service.entities.enums.AddressType;
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
        name = "user_addresses",
        indexes = {
                @Index(name = "ix_user_addresses_user", columnList = "user_id"),
                @Index(name = "ix_user_addresses_user_type", columnList = "user_id, address_type"),
                @Index(name = "ix_user_addresses_user_primary", columnList = "user_id, is_primary")
        }
)
public class UserAddress extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 32)
    private AddressType addressType;

    @Column(name = "address_line_1", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String addressLine1;

    @Column(name = "address_line_2")
    @Size(max = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false)
    @NotBlank
    @Size(max = 128)
    private String city;

    @Column(name = "state", length = 128)
    private String state;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 64)
    @NotBlank
    private String country;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;
}
