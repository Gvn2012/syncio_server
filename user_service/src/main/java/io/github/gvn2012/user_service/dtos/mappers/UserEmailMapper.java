package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserEmailResponse;
import io.github.gvn2012.user_service.entities.UserEmail;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class UserEmailMapper implements IMapper<UserEmail, UserEmailResponse> {
    @Override
    public UserEmailResponse toDto(UserEmail userEmail) {
        return new UserEmailResponse(
                userEmail.getId().toString(),
                userEmail.getEmail(),
                userEmail.getVerified(),
                userEmail.getPrimary(),
                userEmail.getAddedAt(),
                userEmail.getVerifiedAt(),
                userEmail.getStatus()
        );
    }

    @Override
    public UserEmail toEntity(UserEmailResponse dto) {
        UserEmail userEmail = new UserEmail();
        userEmail.setEmail(dto.getEmail());
        userEmail.setVerified(dto.getVerified());
        userEmail.setPrimary(dto.getPrimary());
        userEmail.setAddedAt(dto.getAddedAt());
        userEmail.setVerifiedAt(dto.getVerifiedAt());
        userEmail.setStatus(dto.getStatus());
        return userEmail;
    }
}
