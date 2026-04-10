package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.UpdateProfilePictureRequest;

import java.util.UUID;

public interface IUserProfilePictureService {
    APIResource<Void> updateProfilePicture(UUID userId, UpdateProfilePictureRequest request);
}
