package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.AddressDto;
import io.github.gvn2012.user_service.entities.UserAddress;
import org.springframework.stereotype.Component;

@Component
public class UserAddressMapper implements IMapper<UserAddress, AddressDto> {

    @Override
    public AddressDto toDto(UserAddress entity) {
        return new AddressDto(
                entity.getId().toString(),
                entity.getAddressType(),
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry(),
                entity.getPrimary());
    }

    @Override
    public UserAddress toEntity(AddressDto dto) {
        UserAddress entity = new UserAddress();
        entity.setAddressType(dto.getAddressType());
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
        entity.setPrimary(dto.getPrimary());
        return entity;
    }
}
