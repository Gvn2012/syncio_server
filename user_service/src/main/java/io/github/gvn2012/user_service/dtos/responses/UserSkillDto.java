package io.github.gvn2012.user_service.dtos.responses;

import io.github.gvn2012.user_service.entities.enums.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSkillDto {
    private String id;
    private String skillDefinitionId;
    private String skillName;
    private ProficiencyLevel proficiencyLevel;
    private Integer yearsOfExperience;
    private Boolean verified;
    private String verifiedBy;
    private Instant verifiedAt;
}
