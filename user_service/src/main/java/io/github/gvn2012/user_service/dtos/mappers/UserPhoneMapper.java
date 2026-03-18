package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.UserPhoneResponse;
import io.github.gvn2012.user_service.entities.UserPhone;
import org.springframework.stereotype.Component;


@Component
public class UserPhoneMapper implements IMapper<UserPhone, UserPhoneResponse> {

    @Override
    public UserPhone toEntity(UserPhoneResponse dto) {
        UserPhone entity = new UserPhone();
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setVerified(dto.getVerified());
        entity.setPrimary(dto.getPrimary());
        entity.setVerifiedAt(dto.getVerifiedAt());
        entity.setAddedAt(dto.getAddedAt());
        entity.setMetadata(dto.getMetadata());
        return entity;
    }

    @Override
    public UserPhoneResponse toDto (UserPhone entity) {
        return new UserPhoneResponse(
                entity.getId().toString(),
                entity.getPhoneNumber(),
                entity.getVerified(),
                entity.getPrimary(),
                entity.getAddedAt(),
                entity.getVerifiedAt(),
                entity.getMetadata()
        );
    }
}
