package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.requests.CreatePositionRequest;
import io.github.gvn2012.org_service.dtos.responses.PositionDto;
import io.github.gvn2012.org_service.entities.Position;

public class PositionMapper {

    public static Position toEntity(CreatePositionRequest request) {
        Position entity = new Position();
        entity.setTitle(request.getTitle());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setMinSalary(request.getMinSalary());
        entity.setMaxSalary(request.getMaxSalary());
        entity.setCurrency(request.getCurrency());
        entity.setActive(request.getActive() != null ? request.getActive() : true);
        entity.setRequirements(request.getRequirements());
        return entity;
    }

    public static PositionDto toDto(Position entity) {
        return PositionDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .code(entity.getCode())
                .description(entity.getDescription())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .minSalary(entity.getMinSalary())
                .maxSalary(entity.getMaxSalary())
                .currency(entity.getCurrency())
                .active(entity.getActive())
                .requirements(entity.getRequirements())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
