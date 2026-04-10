package io.github.gvn2012.user_service.dtos.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.gvn2012.user_service.clients.UploadClient;
import io.github.gvn2012.user_service.dtos.responses.UserProfilePictureResponse;
import io.github.gvn2012.user_service.dtos.responses.UserProfileResponse;
import io.github.gvn2012.user_service.entities.UserProfile;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import io.github.gvn2012.user_service.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserProfileMapper implements IMapper<UserProfile, UserProfileResponse> {

    private final UserProfilePictureMapper pictureMapper;
    private final JsonHelper jsonHelper;
    private final UploadClient uploadClient;

    public UserProfileMapper(UserProfilePictureMapper pictureMapper,
                             JsonHelper jsonHelper,
                             UploadClient uploadClient) {
        this.pictureMapper = pictureMapper;
        this.jsonHelper = jsonHelper;
        this.uploadClient = uploadClient;
    }

    @Override
    public UserProfileResponse toDto(UserProfile entity) {
        Set<UserProfilePicture> activePictures = entity.getPictures()
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> objectPaths = activePictures.stream()
                .map(UserProfilePicture::getObjectPath)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> signedUrls = Map.of();
        if (!objectPaths.isEmpty()) {
            try {
                signedUrls = uploadClient.getSignedUrls(objectPaths).block();
                if (signedUrls == null) signedUrls = Map.of();
            } catch (Exception e) {
                log.warn("Failed to resolve signed URLs for profile {}", entity.getId(), e);
            }
        }

        Map<String, String> resolvedUrls = signedUrls;
        Set<UserProfilePictureResponse> pictureResponses = activePictures.stream()
                .map(p -> pictureMapper.toDto(p, resolvedUrls.getOrDefault(p.getObjectPath(), null)))
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
                pictureResponses
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
