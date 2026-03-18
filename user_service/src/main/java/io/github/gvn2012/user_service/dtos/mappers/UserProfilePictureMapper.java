package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserProfilePictureResponse;
import io.github.gvn2012.user_service.dtos.responses.UserProfileResponse;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import org.springframework.stereotype.Component;


@Component
public class UserProfilePictureMapper implements IMapper<UserProfilePicture, UserProfilePictureResponse> {

    @Override
    public UserProfilePictureResponse toDto(UserProfilePicture entity) {
        return new UserProfilePictureResponse(
                entity.getId().toString(),
                entity.getFileSize(),
                entity.getHeight(),
                entity.getWidth(),
                entity.getUrl(),
                entity.getMimeType(),
                entity.getDeleted(),
                entity.getPrimary()
        );
    }

    @Override
    public UserProfilePicture toEntity(UserProfilePictureResponse dto) {
        UserProfilePicture entity = new UserProfilePicture();
        entity.setFileSize(dto.getFileSize());
        entity.setHeight(dto.getHeight());
        entity.setWidth(dto.getWidth());
        entity.setUrl(dto.getUrl());
        entity.setMimeType(dto.getMimeType());
        entity.setDeleted(dto.getDeleted());
        entity.setPrimary(dto.getPrimary());
        return entity;
    }
}
