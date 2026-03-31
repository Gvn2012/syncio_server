package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.OfficeLocationMapper;
import io.github.gvn2012.org_service.dtos.requests.CreateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdateOfficeLocationRequest;
import io.github.gvn2012.org_service.dtos.responses.OfficeLocationDto;
import io.github.gvn2012.org_service.entities.OfficeLocation;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.enums.OfficeStatus;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.OfficeLocationRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.services.interfaces.IOfficeLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficeLocationServiceImpl implements IOfficeLocationService {

    private final OfficeLocationRepository officeLocationRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional
    public OfficeLocationDto createOfficeLocation(UUID orgId, CreateOfficeLocationRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (Boolean.TRUE.equals(request.getHeadquarters())) {
            officeLocationRepository.findByOrganization_IdAndHeadquartersTrue(orgId)
                    .ifPresent(hq -> {
                        hq.setHeadquarters(false);
                        officeLocationRepository.save(hq);
                    });
        }

        OfficeLocation officeLocation = OfficeLocationMapper.toEntity(request);
        officeLocation.setOrganization(organization);

        OfficeLocation savedOfficeLocation = officeLocationRepository.save(officeLocation);
        return OfficeLocationMapper.toDto(savedOfficeLocation);
    }

    @Override
    @Transactional
    public OfficeLocationDto updateOfficeLocation(UUID orgId, UUID officeLocationId, UpdateOfficeLocationRequest request) {
        OfficeLocation officeLocation = officeLocationRepository.findByIdAndOrganization_Id(officeLocationId, orgId)
                .orElseThrow(() -> new NotFoundException("Office location not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            officeLocation.setName(request.getName());
        }

        if (request.getAddressLine1() != null && !request.getAddressLine1().isBlank()) {
            officeLocation.setAddressLine1(request.getAddressLine1());
        }

        if (request.getAddressLine2() != null) {
            officeLocation.setAddressLine2(request.getAddressLine2());
        }

        if (request.getCity() != null && !request.getCity().isBlank()) {
            officeLocation.setCity(request.getCity());
        }

        if (request.getState() != null) {
            officeLocation.setState(request.getState());
        }

        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            officeLocation.setCountry(request.getCountry());
        }

        if (request.getPostalCode() != null) {
            officeLocation.setPostalCode(request.getPostalCode());
        }

        if (request.getPhoneNumber() != null) {
            officeLocation.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getEmail() != null) {
            officeLocation.setEmail(request.getEmail());
        }

        if (request.getCapacity() != null) {
            officeLocation.setCapacity(request.getCapacity());
        }

        if (request.getStatus() != null) {
            officeLocation.setStatus(request.getStatus());
        }

        if (request.getHeadquarters() != null) {
            if (Boolean.TRUE.equals(request.getHeadquarters()) && !Boolean.TRUE.equals(officeLocation.getHeadquarters())) {
                officeLocationRepository.findByOrganization_IdAndHeadquartersTrue(orgId)
                        .ifPresent(hq -> {
                            hq.setHeadquarters(false);
                            officeLocationRepository.save(hq);
                        });
            }
            officeLocation.setHeadquarters(request.getHeadquarters());
        }

        OfficeLocation updatedOfficeLocation = officeLocationRepository.save(officeLocation);
        return OfficeLocationMapper.toDto(updatedOfficeLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeLocationDto getOfficeLocationById(UUID orgId, UUID officeLocationId) {
        OfficeLocation officeLocation = officeLocationRepository.findByIdAndOrganization_Id(officeLocationId, orgId)
                .orElseThrow(() -> new NotFoundException("Office location not found"));
        return OfficeLocationMapper.toDto(officeLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeLocationDto> getOfficeLocationsByOrgId(UUID orgId) {
        return officeLocationRepository.findByOrganization_Id(orgId).stream()
                .map(OfficeLocationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeLocationDto> getOfficeLocationsByOrgIdAndStatus(UUID orgId, OfficeStatus status) {
        return officeLocationRepository.findByOrganization_IdAndStatus(orgId, status).stream()
                .map(OfficeLocationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfficeLocationDto getHeadquartersByOrgId(UUID orgId) {
        OfficeLocation hq = officeLocationRepository.findByOrganization_IdAndHeadquartersTrue(orgId)
                .orElseThrow(() -> new NotFoundException("Headquarters not found for this organization"));
        return OfficeLocationMapper.toDto(hq);
    }

    @Override
    @Transactional
    public void deleteOfficeLocation(UUID orgId, UUID officeLocationId) {
        OfficeLocation officeLocation = officeLocationRepository.findByIdAndOrganization_Id(officeLocationId, orgId)
                .orElseThrow(() -> new NotFoundException("Office location not found"));
        officeLocationRepository.delete(officeLocation);
    }
}
