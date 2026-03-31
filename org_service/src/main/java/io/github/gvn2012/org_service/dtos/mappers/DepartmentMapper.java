package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.responses.DepartmentDto;
import io.github.gvn2012.org_service.entities.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper implements IMapper<Department, DepartmentDto> {

    @Override
    public DepartmentDto toDto(Department entity) {
        if (entity == null) {
            return null;
        }

        return DepartmentDto.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganization() != null ? entity.getOrganization().getId() : null)
                .parentDepartmentId(entity.getParentDepartment() != null ? entity.getParentDepartment().getId() : null)
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .headOfDepartmentId(entity.getHeadOfDepartmentId())
                .status(entity.getStatus())
                .budget(entity.getBudget())
                .costCenterCode(entity.getCostCenterCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Department toEntity(DepartmentDto dto) {
        if (dto == null) {
            return null;
        }

        Department entity = new Department();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setDescription(dto.getDescription());
        entity.setHeadOfDepartmentId(dto.getHeadOfDepartmentId());
        entity.setStatus(dto.getStatus());
        entity.setBudget(dto.getBudget());
        entity.setCostCenterCode(dto.getCostCenterCode());
        return entity;
    }
}
