package io.github.gvn2012.user_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgRegisterDTO {
    private String name;
    private String legalName;
    private String description;
    private String industry;
    private String website;
    private String logoUrl;
    private String foundedDate;
    private String registrationNumber;
    private String taxId;
    private String organizationSize;
    private String parentOrganizationId;
}
