package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.responses.TeamDto;
import io.github.gvn2012.org_service.entities.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper implements IMapper<Team, TeamDto> {

    @Override
    public TeamDto toDto(Team entity) {
        if (entity == null) {
            return null;
        }

        return TeamDto.builder()
                .id(entity.getId())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .teamLeadId(entity.getTeamLeadId())
                .status(entity.getStatus())
                .maxCapacity(entity.getMaxCapacity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public Team toEntity(TeamDto dto) {
        if (dto == null) {
            return null;
        }

        Team entity = new Team();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setTeamLeadId(dto.getTeamLeadId());
        entity.setStatus(dto.getStatus());
        entity.setMaxCapacity(dto.getMaxCapacity());
        return entity;
    }
}
