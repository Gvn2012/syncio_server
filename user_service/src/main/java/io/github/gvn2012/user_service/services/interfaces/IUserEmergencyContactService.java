package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmergencyContactRequest;
import io.github.gvn2012.user_service.dtos.responses.*;

import java.util.UUID;

public interface IUserEmergencyContactService {

    APIResource<GetUserEmergencyContactResponse> getUserEmergencyContact(String userId);

    APIResource<AddNewEmergencyContactResponse> addNewEmergencyContact(UUID userId, AddNewEmergencyContactRequest request);

    APIResource<UpdateEmergencyContactResponse> updateEmergencyContact(UUID userId, UUID contactId, UpdateEmergencyContactRequest request);

    APIResource<DeleteEmergencyContactResponse> deleteEmergencyContact(UUID userId, UUID contactId);

    APIResource<SetPrimaryEmergencyContactResponse> setPrimaryEmergencyContact(UUID userId, UUID contactId);
}
