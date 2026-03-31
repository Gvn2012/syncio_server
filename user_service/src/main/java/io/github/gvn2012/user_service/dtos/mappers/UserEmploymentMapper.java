package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserEmploymentDto;
import io.github.gvn2012.user_service.entities.UserEmployment;
import org.springframework.stereotype.Component;

@Component
public class UserEmploymentMapper implements IMapper<UserEmployment, UserEmploymentDto> {

    @Override
    public UserEmploymentDto toDto(UserEmployment entity) {
        return new UserEmploymentDto(
                entity.getId().toString(),
                entity.getOrganizationId() != null ? entity.getOrganizationId().toString() : null,
                entity.getDepartmentId() != null ? entity.getDepartmentId().toString() : null,
                entity.getTeamId() != null ? entity.getTeamId().toString() : null,
                entity.getEmployeeCode(),
                entity.getJobTitleId() != null ? entity.getJobTitleId().toString() : null,
                entity.getJobLevelId() != null ? entity.getJobLevelId().toString() : null,
                entity.getEmploymentType(),
                entity.getEmploymentStatus(),
                entity.getManagerId() != null ? entity.getManagerId().toString() : null,
                entity.getHireDate(),
                entity.getProbationEndDate(),
                entity.getTerminationDate(),
                entity.getWorkLocation(),
                entity.getCurrent());
    }

    @Override
    public UserEmployment toEntity(UserEmploymentDto dto) {
        UserEmployment entity = new UserEmployment();
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setEmploymentStatus(dto.getEmploymentStatus());
        entity.setHireDate(dto.getHireDate());
        entity.setProbationEndDate(dto.getProbationEndDate());
        entity.setTerminationDate(dto.getTerminationDate());
        entity.setWorkLocation(dto.getWorkLocation());
        entity.setCurrent(dto.getCurrent());
        return entity;
    }
}
