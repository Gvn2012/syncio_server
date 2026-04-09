package io.github.gvn2012.user_service.dtos.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.user_service.dtos.responses.UserEmailResponse;
import io.github.gvn2012.user_service.entities.UserEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserEmailMapper implements IMapper<UserEmail, UserEmailResponse> {

    private final ObjectMapper objectMapper;

    @Override
    public UserEmailResponse toDto(UserEmail userEmail) {
        Map<String, Object> metadata = null;
        if (userEmail.getMetadata() != null && !userEmail.getMetadata().isBlank()) {
            try {
                metadata = objectMapper.readValue(userEmail.getMetadata(), new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception e) {
                // Log error or fallback
            }
        }

        return UserEmailResponse.builder()
                .id(userEmail.getId().toString())
                .email(userEmail.getEmail())
                .verified(userEmail.getVerified())
                .primary(userEmail.getPrimary())
                .verifiedAt(userEmail.getVerifiedAt())
                .status(userEmail.getStatus())
                .metadata(metadata)
                .build();
    }

    @Override
    public UserEmail toEntity(UserEmailResponse dto) {
        UserEmail userEmail = new UserEmail();
        userEmail.setEmail(dto.getEmail());
        userEmail.setVerified(dto.getVerified());
        userEmail.setPrimary(dto.getPrimary());
        userEmail.setVerifiedAt(dto.getVerifiedAt());
        userEmail.setStatus(dto.getStatus());

        if (dto.getMetadata() != null) {
            try {
                userEmail.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            } catch (Exception e) {
                // Log error
            }
        }
        return userEmail;
    }
}
