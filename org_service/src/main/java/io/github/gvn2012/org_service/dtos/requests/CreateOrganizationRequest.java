package io.github.gvn2012.org_service.dtos.requests;

import io.github.gvn2012.org_service.entities.enums.OrganizationSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String legalName;

    private String description;

    @Size(max = 128)
    private String industry;

    @Size(max = 512)
    private String website;

    @Size(max = 1024)
    private String logoUrl;
    private String logoPath;

    private LocalDate foundedDate;

    @Size(max = 128)
    private String registrationNumber;

    @Size(max = 128)
    private String taxId;

    private OrganizationSize organizationSize;

    @Size(max = 255)
    private String address;

    @Size(max = 128)
    private String city;

    @Size(max = 128)
    private String state;

    @Size(max = 64)
    private String country;

    @Size(max = 32)
    private String postalCode;

    @Size(max = 36)
    private String ownerId;

    @NotBlank(message = "Email is required")
    @Size(max = 255)
    private String email;
}
