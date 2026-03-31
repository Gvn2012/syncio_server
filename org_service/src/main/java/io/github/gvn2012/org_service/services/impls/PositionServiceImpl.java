package io.github.gvn2012.org_service.services.impls;

import io.github.gvn2012.org_service.dtos.mappers.PositionMapper;
import io.github.gvn2012.org_service.dtos.requests.CreatePositionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdatePositionRequest;
import io.github.gvn2012.org_service.dtos.responses.PositionDto;
import io.github.gvn2012.org_service.entities.Department;
import io.github.gvn2012.org_service.entities.Organization;
import io.github.gvn2012.org_service.entities.Position;
import io.github.gvn2012.org_service.exceptions.BadRequestException;
import io.github.gvn2012.org_service.exceptions.NotFoundException;
import io.github.gvn2012.org_service.repositories.DepartmentRepository;
import io.github.gvn2012.org_service.repositories.OrganizationRepository;
import io.github.gvn2012.org_service.repositories.PositionRepository;
import io.github.gvn2012.org_service.services.interfaces.IPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements IPositionService {

    private final PositionRepository positionRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public PositionDto createPosition(UUID orgId, CreatePositionRequest request) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (positionRepository.findByOrganization_IdAndCode(orgId, request.getCode()).isPresent()) {
            throw new BadRequestException("Position code already exists in this organization");
        }

        Position position = PositionMapper.toEntity(request);
        position.setOrganization(organization);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            position.setDepartment(department);
        }

        Position savedPosition = positionRepository.save(position);
        return PositionMapper.toDto(savedPosition);
    }

    @Override
    @Transactional
    public PositionDto updatePosition(UUID orgId, UUID positionId, UpdatePositionRequest request) {
        Position position = positionRepository.findByIdAndOrganization_Id(positionId, orgId)
                .orElseThrow(() -> new NotFoundException("Position not found"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            position.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            position.setDescription(request.getDescription());
        }

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization_Id(request.getDepartmentId(), orgId)
                    .orElseThrow(() -> new NotFoundException("Department not found in this organization"));
            position.setDepartment(department);
        }

        if (request.getMinSalary() != null) {
            position.setMinSalary(request.getMinSalary());
        }

        if (request.getMaxSalary() != null) {
            position.setMaxSalary(request.getMaxSalary());
        }

        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            position.setCurrency(request.getCurrency());
        }

        if (request.getActive() != null) {
            position.setActive(request.getActive());
        }

        if (request.getRequirements() != null) {
            position.setRequirements(request.getRequirements());
        }

        Position updatedPosition = positionRepository.save(position);
        return PositionMapper.toDto(updatedPosition);
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDto getPositionById(UUID orgId, UUID positionId) {
        Position position = positionRepository.findByIdAndOrganization_Id(positionId, orgId)
                .orElseThrow(() -> new NotFoundException("Position not found"));
        return PositionMapper.toDto(position);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDto> getPositionsByOrgId(UUID orgId) {
        return positionRepository.findByOrganization_IdAndActiveTrue(orgId).stream()
                .map(PositionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionDto> getPositionsByOrgIdAndDepartmentId(UUID orgId, UUID departmentId) {
        return positionRepository.findByOrganization_IdAndDepartment_Id(orgId, departmentId).stream()
                .filter(Position::getActive)
                .map(PositionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePosition(UUID orgId, UUID positionId) {
        Position position = positionRepository.findByIdAndOrganization_Id(positionId, orgId)
                .orElseThrow(() -> new NotFoundException("Position not found"));
        position.setActive(false);
        positionRepository.save(position);
    }
}
