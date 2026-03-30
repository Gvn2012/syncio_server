package io.github.gvn2012.org_service.entities;

import io.github.gvn2012.org_service.entities.enums.OrganizationSize;
import io.github.gvn2012.org_service.entities.enums.OrganizationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "organizations",
        indexes = {
                @Index(name = "ix_organizations_status", columnList = "status"),
                @Index(name = "ix_organizations_owner", columnList = "owner_id"),
                @Index(name = "ix_organizations_parent", columnList = "parent_organization_id"),
                @Index(name = "ix_organizations_industry", columnList = "industry")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organizations_slug", columnNames = "slug")
        }
)
public class Organization extends AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "legal_name")
    @Size(max = 255)
    private String legalName;

    @Column(name = "slug", nullable = false, length = 128)
    @NotBlank
    @Size(max = 128)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "industry", length = 128)
    private String industry;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "logo_url", length = 1024)
    private String logoUrl;

    @Column(name = "founded_date")
    private LocalDate foundedDate;

    @Column(name = "registration_number", length = 128)
    private String registrationNumber;

    @Column(name = "tax_id", length = 128)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_size", length = 32)
    private OrganizationSize organizationSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    /** References User.id in user_service */
    @Column(name = "owner_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID ownerId;

    /** Self-reference for subsidiary/parent org hierarchy */
    @Column(name = "parent_organization_id", columnDefinition = "BINARY(16)")
    private UUID parentOrganizationId;

    @Column(name = "address")
    @Size(max = 255)
    private String address;

    @Column(name = "city", length = 128)
    private String city;

    @Column(name = "state", length = 128)
    private String state;

    @Column(name = "country", length = 64)
    private String country;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Department> departments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrganizationMember> members = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrgJobTitle> jobTitles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrgJobLevel> jobLevels = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrgSkillDefinition> skillDefinitions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Position> positions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OfficeLocation> officeLocations = new LinkedHashSet<>();
}
