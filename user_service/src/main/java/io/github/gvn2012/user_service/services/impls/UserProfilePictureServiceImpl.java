package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.UpdateProfilePictureRequest;
import io.github.gvn2012.user_service.entities.UserProfile;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserProfilePictureRepository;
import io.github.gvn2012.user_service.repositories.UserProfileRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserProfilePictureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfilePictureServiceImpl implements IUserProfilePictureService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfilePictureRepository userProfilePictureRepository;

    @Override
    @Transactional
    public APIResource<Void> updateProfilePicture(UUID userId, UpdateProfilePictureRequest request) {
        log.info("Updating profile picture for userId: {} with imageId: {}", userId, request.getImageId());

        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        // Deactivate current primary pictures
        profile.getPictures().stream()
                .filter(UserProfilePicture::getPrimary)
                .forEach(p -> p.setPrimary(false));

        // Create new pending picture record
        UserProfilePicture newPicture = new UserProfilePicture();
        newPicture.setUserProfile(profile);
        newPicture.setExternalId(request.getImageId().toString());
        newPicture.setPrimary(true);
        newPicture.setDeleted(false);
        newPicture.setUrl("PENDING");

        profile.getPictures().add(newPicture);
        userProfileRepository.save(profile);

        log.info("Successfully initialized new profile picture record in PENDING state");
        return APIResource.ok("Profile picture update initiated successfully", null);
    }
}
