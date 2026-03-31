package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.responses.OfficeLocationDto;
import io.github.gvn2012.org_service.entities.enums.OfficeStatus;

import java.util.List;
import java.util.UUID;

public interface IOfficeLocationService {
    OfficeLocationDto createOfficeLocation(UUID orgId, CreateOfficeLocationRequest request);
    OfficeLocationDto updateOfficeLocation(UUID orgId, UUID officeLocationId, UpdateOfficeLocationRequest request);
    OfficeLocationDto getOfficeLocationById(UUID orgId, UUID officeLocationId);
    List<OfficeLocationDto> getOfficeLocationsByOrgId(UUID orgId);
    List<OfficeLocationDto> getOfficeLocationsByOrgIdAndStatus(UUID orgId, OfficeStatus status);
    OfficeLocationDto getHeadquartersByOrgId(UUID orgId);
    void deleteOfficeLocation(UUID orgId, UUID officeLocationId);
}
