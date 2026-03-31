package io.github.gvn2012.org_service.dtos.mappers;

import io.github.gvn2012.org_service.dtos.responses.TeamMemberDto;
import io.github.gvn2012.org_service.entities.TeamMember;
import org.springframework.stereotype.Component;

@Component
public class TeamMemberMapper implements IMapper<TeamMember, TeamMemberDto> {

    @Override
    public TeamMemberDto toDto(TeamMember entity) {
        if (entity == null) {
            return null;
        }

        return TeamMemberDto.builder()
                .id(entity.getId())
                .teamId(entity.getTeam() != null ? entity.getTeam().getId() : null)
                .userId(entity.getUserId())
                .teamRole(entity.getTeamRole())
                .joinedAt(entity.getJoinedAt())
                .leftAt(entity.getLeftAt())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public TeamMember toEntity(TeamMemberDto dto) {
        if (dto == null) {
            return null;
        }

        TeamMember entity = new TeamMember();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setTeamRole(dto.getTeamRole());
        entity.setJoinedAt(dto.getJoinedAt());
        entity.setLeftAt(dto.getLeftAt());
        entity.setActive(dto.getActive());
        return entity;
    }
}
