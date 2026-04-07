package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserProfilePictureResponse;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class UserProfilePictureMapper implements IMapper<UserProfilePicture, UserProfilePictureResponse> {

    @Value("${gcp.storage.public-url-prefix:https://storage.googleapis.com}")
    private String publicUrlPrefix;

    @Override
    public UserProfilePictureResponse toDto(UserProfilePicture entity) {
        String url = entity.getUrl();
        if (entity.getObjectPath() != null && entity.getBucketName() != null) {
            url = String.format("%s/%s/%s", publicUrlPrefix, entity.getBucketName(), entity.getObjectPath());
        }

        return new UserProfilePictureResponse(
                entity.getId().toString(),
                entity.getFileSize(),
                entity.getHeight(),
                entity.getWidth(),
                url,
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
