package io.github.gvn2012.user_service.dtos.mappers;

import io.github.gvn2012.user_service.dtos.responses.EmergencyContactDto;
import io.github.gvn2012.user_service.entities.UserEmergencyContact;
import org.springframework.stereotype.Component;

@Component
public class UserEmergencyContactMapper implements IMapper<UserEmergencyContact, EmergencyContactDto> {

    @Override
    public EmergencyContactDto toDto(UserEmergencyContact entity) {
        return new EmergencyContactDto(
                entity.getId().toString(),
                entity.getContactName(),
                entity.getRelationship(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getPrimary(),
                entity.getPriority());
    }

    @Override
    public UserEmergencyContact toEntity(EmergencyContactDto dto) {
        UserEmergencyContact entity = new UserEmergencyContact();
        entity.setContactName(dto.getContactName());
        entity.setRelationship(dto.getRelationship());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());
        entity.setPrimary(dto.getPrimary());
        entity.setPriority(dto.getPriority());
        return entity;
    }
}
