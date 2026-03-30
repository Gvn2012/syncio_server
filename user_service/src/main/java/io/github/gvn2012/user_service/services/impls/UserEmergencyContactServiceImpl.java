package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserEmergencyContact;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserEmergencyContactRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserEmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEmergencyContactServiceImpl implements IUserEmergencyContactService {

    private final UserEmergencyContactRepository emergencyContactRepository;
    private final UserRepository userRepository;

    @Override
    public APIResource<GetUserEmergencyContactResponse> getUserEmergencyContact(String userId) {
        List<EmergencyContactDto> contacts = emergencyContactRepository
                .findByUser_IdOrderByPriorityAsc(UUID.fromString(userId))
                .stream()
                .map(contact -> new EmergencyContactDto(
                        contact.getId().toString(),
                        contact.getContactName(),
                        contact.getRelationship(),
                        contact.getPhoneNumber(),
                        contact.getEmail(),
                        contact.getPrimary(),
                        contact.getPriority()))
                .collect(Collectors.toList());

        return APIResource.ok(
                "Get user emergency contacts successfully",
                new GetUserEmergencyContactResponse(contacts));
    }

    @Override
    @Transactional
    public APIResource<AddNewEmergencyContactResponse> addNewEmergencyContact(UUID userId, AddNewEmergencyContactRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserEmergencyContact contact = new UserEmergencyContact();
        contact.setUser(user);
        contact.setContactName(request.getContactName());
        contact.setRelationship(request.getRelationship());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setEmail(request.getEmail());
        contact.setPrimary(false);
        contact.setPriority(request.getPriority() != null ? request.getPriority() : 1);

        emergencyContactRepository.save(contact);

        return APIResource.ok(
                "Emergency contact added successfully",
                new AddNewEmergencyContactResponse(contact.getId().toString()),
                HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public APIResource<UpdateEmergencyContactResponse> updateEmergencyContact(UUID userId, UUID contactId, UpdateEmergencyContactRequest request) {
        UserEmergencyContact contact = emergencyContactRepository
                .findByIdAndUser_Id(contactId, userId)
                .orElseThrow(() -> new NotFoundException("Emergency contact not found"));

        if (request.getContactName() != null) {
            contact.setContactName(request.getContactName());
        }
        if (request.getRelationship() != null) {
            contact.setRelationship(request.getRelationship());
        }
        if (request.getPhoneNumber() != null) {
            contact.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            contact.setEmail(request.getEmail());
        }
        if (request.getPriority() != null) {
            contact.setPriority(request.getPriority());
        }

        emergencyContactRepository.save(contact);

        return APIResource.ok(
                "Emergency contact updated successfully",
                new UpdateEmergencyContactResponse(contact.getId().toString()));
    }

    @Override
    @Transactional
    public APIResource<DeleteEmergencyContactResponse> deleteEmergencyContact(UUID userId, UUID contactId) {
        UserEmergencyContact contact = emergencyContactRepository
                .findByIdAndUser_Id(contactId, userId)
                .orElseThrow(() -> new NotFoundException("Emergency contact not found"));

        if (Boolean.TRUE.equals(contact.getPrimary())) {
            long count = emergencyContactRepository.findByUser_IdOrderByPriorityAsc(userId).size();
            if (count > 1) {
                throw new BadRequestException("Cannot delete primary emergency contact. Set another contact as primary first.");
            }
        }

        emergencyContactRepository.delete(contact);

        return APIResource.ok(
                "Emergency contact deleted successfully",
                new DeleteEmergencyContactResponse(contact.getId().toString()));
    }

    @Override
    @Transactional
    public APIResource<SetPrimaryEmergencyContactResponse> setPrimaryEmergencyContact(UUID userId, UUID contactId) {
        UserEmergencyContact contact = emergencyContactRepository
                .findByIdAndUser_Id(contactId, userId)
                .orElseThrow(() -> new NotFoundException("Emergency contact not found"));

        if (Boolean.TRUE.equals(contact.getPrimary())) {
            throw new BadRequestException("This contact is already the primary emergency contact");
        }

        String previousPrimaryId = null;
        var currentPrimary = emergencyContactRepository.findByUser_IdAndPrimaryTrue(userId);
        if (currentPrimary.isPresent()) {
            previousPrimaryId = currentPrimary.get().getId().toString();
            currentPrimary.get().setPrimary(false);
            emergencyContactRepository.save(currentPrimary.get());
        }

        contact.setPrimary(true);
        emergencyContactRepository.save(contact);

        return APIResource.ok(
                "Primary emergency contact updated successfully",
                new SetPrimaryEmergencyContactResponse(contact.getId().toString(), previousPrimaryId));
    }
}
