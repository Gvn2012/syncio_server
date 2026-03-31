package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserDetailMapper {

    private final UserMapper userMapper;
    private final UserEmailMapper emailMapper;
    private final UserPhoneMapper phoneMapper;
    private final UserProfileMapper profileMapper;
    private final UserAddressMapper addressMapper;
    private final UserEmergencyContactMapper emergencyContactMapper;
    private final UserEmploymentMapper employmentMapper;
    private final UserSkillMapper skillMapper;
    private final UserPreferenceMapper preferenceMapper;

    public UserDetailMapper(UserMapper userMapper,
                            UserEmailMapper emailMapper,
                            UserPhoneMapper phoneMapper,
                            UserProfileMapper profileMapper,
                            UserAddressMapper addressMapper,
                            UserEmergencyContactMapper emergencyContactMapper,
                            UserEmploymentMapper employmentMapper,
                            UserSkillMapper skillMapper,
                            UserPreferenceMapper preferenceMapper) {
        this.userMapper = userMapper;
        this.emailMapper = emailMapper;
        this.phoneMapper = phoneMapper;
        this.profileMapper = profileMapper;
        this.addressMapper = addressMapper;
        this.emergencyContactMapper = emergencyContactMapper;
        this.employmentMapper = employmentMapper;
        this.skillMapper = skillMapper;
        this.preferenceMapper = preferenceMapper;
    }

    public GetUserDetailResponse toDto(User user) {
        return new GetUserDetailResponse(
                userMapper.toDto(user),
                emailMapper.toDtoSet(user.getEmails()),
                phoneMapper.toDtoSet(user.getPhones()),
                user.getProfile() != null
                        ? profileMapper.toDto(user.getProfile())
                        : null,
                addressMapper.toDtoSet(user.getAddresses()),
                emergencyContactMapper.toDtoSet(user.getEmergencyContacts()),
                employmentMapper.toDtoSet(user.getEmployments()),
                skillMapper.toDtoSet(user.getSkills()),
                preferenceMapper.toDtoSet(user.getPreferences())
        );
    }
}