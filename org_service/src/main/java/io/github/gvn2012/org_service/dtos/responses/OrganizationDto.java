package io.github.gvn2012.org_service.dtos.responses;

import io.github.gvn2012.org_service.entities.enums.OrganizationSize;
import io.github.gvn2012.org_service.entities.enums.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    private UUID id;
    private String name;
    private String legalName;
    private String slug;
    private String description;
    private String industry;
    private String website;
    private String logoUrl;
    private LocalDate foundedDate;
    private String registrationNumber;
    private String taxId;
    private OrganizationSize organizationSize;
    private OrganizationStatus status;
    private UUID ownerId;
    private UUID parentOrganizationId;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Instant createdAt;
    private Instant updatedAt;
}
