package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserResponse;
import io.github.gvn2012.user_service.entities.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper implements IMapper<User, UserResponse> {
    @Override
    public UserResponse toDto(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getGender(),
                user.getLocale(),
                user.getTimezone(),
                user.getActive(),
                user.getSuspended(),
                user.getBanned(),
                user.getMustChangePassword()
        );
    }

    @Override
    public User toEntity(UserResponse dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMiddleName(dto.getMiddleName());
        user.setGender(dto.getGender());
        user.setLocale(dto.getLocale());
        user.setTimezone(dto.getTimezone());
        return user;
    }
}
