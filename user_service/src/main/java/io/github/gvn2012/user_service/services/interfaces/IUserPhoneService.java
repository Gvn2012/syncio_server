package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewPhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdatePhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPhoneRequest;
import io.github.gvn2012.user_service.dtos.responses.*;

import java.util.UUID;

public interface IUserPhoneService {

    APIResource<GetUserPhoneResponse> getUserPhone(String userId);

    APIResource<AddNewPhoneResponse> addNewPhone(UUID userId, AddNewPhoneRequest request);

    APIResource<UpdatePhoneResponse> updatePhone(UUID userId, UUID phoneId, UpdatePhoneRequest request);

    APIResource<VerifyPhoneResponse> verifyPhone(UUID userId, UUID phoneId, VerifyPhoneRequest request);

    APIResource<DeletePhoneResponse> deletePhone(UUID userId, UUID phoneId);

    APIResource<SetPrimaryPhoneResponse> setPrimaryPhone(UUID userId, UUID phoneId);

    APIResource<Void> resendVerificationCode(UUID userId, UUID phoneId);
}
