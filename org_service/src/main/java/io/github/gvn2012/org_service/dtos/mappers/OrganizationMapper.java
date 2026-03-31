package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.responses.OrganizationDto;
import io.github.gvn2012.org_service.entities.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper implements IMapper<Organization, OrganizationDto> {

    @Override
    public OrganizationDto toDto(Organization entity) {
        if (entity == null) {
            return null;
        }

        return OrganizationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .legalName(entity.getLegalName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .industry(entity.getIndustry())
                .website(entity.getWebsite())
                .logoUrl(entity.getLogoUrl())
                .foundedDate(entity.getFoundedDate())
                .registrationNumber(entity.getRegistrationNumber())
                .taxId(entity.getTaxId())
                .organizationSize(entity.getOrganizationSize())
                .status(entity.getStatus())
                .ownerId(entity.getOwnerId())
                .parentOrganizationId(entity.getParentOrganizationId())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Organization toEntity(OrganizationDto dto) {
        if (dto == null) {
            return null;
        }

        Organization entity = new Organization();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setLegalName(dto.getLegalName());
        entity.setSlug(dto.getSlug());
        entity.setDescription(dto.getDescription());
        entity.setIndustry(dto.getIndustry());
        entity.setWebsite(dto.getWebsite());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setFoundedDate(dto.getFoundedDate());
        entity.setRegistrationNumber(dto.getRegistrationNumber());
        entity.setTaxId(dto.getTaxId());
        entity.setOrganizationSize(dto.getOrganizationSize());
        entity.setStatus(dto.getStatus());
        entity.setOwnerId(dto.getOwnerId());
        entity.setParentOrganizationId(dto.getParentOrganizationId());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPostalCode(dto.getPostalCode());
        return entity;
    }
}
