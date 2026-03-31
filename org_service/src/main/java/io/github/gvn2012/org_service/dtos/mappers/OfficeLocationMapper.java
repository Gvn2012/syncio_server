package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.responses.OfficeLocationDto;
import io.github.gvn2012.org_service.entities.OfficeLocation;
import io.github.gvn2012.org_service.entities.enums.OfficeStatus;

public class OfficeLocationMapper {

    public static OfficeLocation toEntity(CreateOfficeLocationRequest request) {
        OfficeLocation entity = new OfficeLocation();
        entity.setName(request.getName());
        entity.setAddressLine1(request.getAddressLine1());
        entity.setAddressLine2(request.getAddressLine2());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setCountry(request.getCountry());
        entity.setPostalCode(request.getPostalCode());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());
        entity.setHeadquarters(request.getHeadquarters() != null ? request.getHeadquarters() : false);
        entity.setCapacity(request.getCapacity());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : OfficeStatus.OPEN);
        return entity;
    }

    public static OfficeLocationDto toDto(OfficeLocation entity) {
        return OfficeLocationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .country(entity.getCountry())
                .postalCode(entity.getPostalCode())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .headquarters(entity.getHeadquarters())
                .capacity(entity.getCapacity())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
