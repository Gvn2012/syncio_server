package io.github.gvn2012.user_service.dtos.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.user_service.dtos.responses.UserProfilePictureResponse;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserProfilePictureMapper implements IMapper<UserProfilePicture, UserProfilePictureResponse> {

    private final ObjectMapper objectMapper;

    @Value("${gcp.storage.public-url-prefix:https://storage.googleapis.com}")
    private String publicUrlPrefix;

    @Override
    public UserProfilePictureResponse toDto(UserProfilePicture entity) {
        String url = entity.getUrl();
        if (entity.getObjectPath() != null && entity.getBucketName() != null) {
            url = String.format("%s/%s/%s", publicUrlPrefix, entity.getBucketName(), entity.getObjectPath());
        }

        Map<String, Object> metadata = null;
        if (entity.getMetadata() != null && !entity.getMetadata().isBlank()) {
            try {
                metadata = objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
            }
        }

        return UserProfilePictureResponse.builder()
                .id(entity.getId().toString())
                .fileSize(entity.getFileSize())
                .height(entity.getHeight())
                .width(entity.getWidth())
                .url(url)
                .objectPath(entity.getObjectPath())
                .bucketName(entity.getBucketName())
                .mimeType(entity.getMimeType())
                .deleted(entity.getDeleted())
                .primary(entity.getPrimary())
                .metadata(metadata)
                .build();
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
        entity.setBucketName(dto.getBucketName());
        entity.setObjectPath(dto.getObjectPath());

        if (dto.getMetadata() != null) {
            try {
                entity.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            } catch (Exception e) {
            }
        }

        return entity;
    }
}
