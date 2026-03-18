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

    public UserDetailMapper(UserMapper userMapper,
                            UserEmailMapper emailMapper,
                            UserPhoneMapper phoneMapper,
                            UserProfileMapper profileMapper) {
        this.userMapper = userMapper;
        this.emailMapper = emailMapper;
        this.phoneMapper = phoneMapper;
        this.profileMapper = profileMapper;
    }

    public GetUserDetailResponse toDto(User user) {
        return new GetUserDetailResponse(
                userMapper.toDto(user),
                emailMapper.toDtoList(user.getEmails()),
                phoneMapper.toDtoList(user.getPhones()),
                user.getProfile() != null
                        ? profileMapper.toDto(user.getProfile())
                        : null
        );
    }
}