package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserPreferenceDto;
import io.github.gvn2012.user_service.entities.UserPreference;
import org.springframework.stereotype.Component;

@Component
public class UserPreferenceMapper implements IMapper<UserPreference, UserPreferenceDto> {

    @Override
    public UserPreferenceDto toDto(UserPreference entity) {
        return new UserPreferenceDto(
                entity.getId().toString(),
                entity.getCategory(),
                entity.getPreferenceKey(),
                entity.getPreferenceValue());
    }

    @Override
    public UserPreference toEntity(UserPreferenceDto dto) {
        UserPreference entity = new UserPreference();
        entity.setCategory(dto.getCategory());
        entity.setPreferenceKey(dto.getPreferenceKey());
        entity.setPreferenceValue(dto.getPreferenceValue());
        return entity;
    }
}
