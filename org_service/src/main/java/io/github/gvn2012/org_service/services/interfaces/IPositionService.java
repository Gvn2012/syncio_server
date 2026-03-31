package io.github.gvn2012.org_service.services.interfaces;

import io.github.gvn2012.org_service.dtos.requests.CreatePositionRequest;
import io.github.gvn2012.org_service.dtos.requests.UpdatePositionRequest;
import io.github.gvn2012.org_service.dtos.responses.PositionDto;

import java.util.List;
import java.util.UUID;

public interface IPositionService {
    PositionDto createPosition(UUID orgId, CreatePositionRequest request);
    PositionDto updatePosition(UUID orgId, UUID positionId, UpdatePositionRequest request);
    PositionDto getPositionById(UUID orgId, UUID positionId);
    List<PositionDto> getPositionsByOrgId(UUID orgId);
    List<PositionDto> getPositionsByOrgIdAndDepartmentId(UUID orgId, UUID departmentId);
    void deletePosition(UUID orgId, UUID positionId);
}
