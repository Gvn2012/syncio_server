package io.github.gvn2012.user_service.dtos.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.gvn2012.user_service.dtos.responses.UserProfileResponse;
import io.github.gvn2012.user_service.entities.UserProfile;
import io.github.gvn2012.user_service.utils.JsonHelper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserProfileMapper implements IMapper<UserProfile, UserProfileResponse> {

    private final UserProfilePictureMapper pictureMapper;
    private final JsonHelper jsonHelper;

    public UserProfileMapper(UserProfilePictureMapper pictureMapper,
                             JsonHelper jsonHelper) {
        this.pictureMapper = pictureMapper;
        this.jsonHelper = jsonHelper;
    }

    @Override
    public UserProfileResponse toDto(UserProfile entity) {
        return new UserProfileResponse(
                entity.getId().toString(),
                entity.getDateOfBirth(),
                entity.getBio(),
                entity.getLocation(),
                jsonHelper.fromJson(
                        entity.getContactInfo(),
                        new TypeReference<Map<String, String>>() {},
                        Map.of()
                ),
                entity.getProfileCompletedScore(),
                pictureMapper.toDtoSet(
                        entity.getPictures()
                                .stream()
                                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                )
        );
    }

    @Override
    public UserProfile toEntity(UserProfileResponse dto) {
        UserProfile entity = new UserProfile();
        entity.setBio(dto.getBio());
        entity.setLocation(dto.getLocation());

        entity.setDateOfBirth(dto.getDateOfBirth());

        entity.setContactInfo(
                jsonHelper.toJson(dto.getContactInfo(), "{}")
        );

        entity.setProfileCompletedScore(dto.getProfileCompletedScore());

        entity.setPictures(
                pictureMapper.toEntitySet(dto.getUserProfilePictureResponseList())
        );

        entity.getPictures().forEach(p -> p.setUserProfile(entity));

        return entity;
    }
}
