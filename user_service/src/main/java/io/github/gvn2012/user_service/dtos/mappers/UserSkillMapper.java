package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserSkillDto;
import io.github.gvn2012.user_service.entities.UserSkill;
import org.springframework.stereotype.Component;

@Component
public class UserSkillMapper implements IMapper<UserSkill, UserSkillDto> {

    @Override
    public UserSkillDto toDto(UserSkill entity) {
        return new UserSkillDto(
                entity.getId().toString(),
                entity.getSkillDefinitionId() != null ? entity.getSkillDefinitionId().toString() : null,
                entity.getSkillName(),
                entity.getProficiencyLevel(),
                entity.getYearsOfExperience(),
                entity.getVerified(),
                entity.getVerifiedBy() != null ? entity.getVerifiedBy().toString() : null,
                entity.getVerifiedAt());
    }

    @Override
    public UserSkill toEntity(UserSkillDto dto) {
        UserSkill entity = new UserSkill();
        entity.setSkillName(dto.getSkillName());
        entity.setProficiencyLevel(dto.getProficiencyLevel());
        entity.setYearsOfExperience(dto.getYearsOfExperience());
        entity.setVerified(dto.getVerified());
        return entity;
    }
}
