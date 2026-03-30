package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.DeleteEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.*;

import java.util.UUID;

public interface IUserEmailService {

    APIResource<GetUserEmailResponse> getUserEmail(String userId);

    APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request);

    APIResource<UpdateEmailResponse> updateEmail(UUID userId, UUID emailId, UpdateEmailRequest request);

    APIResource<VerifyEmailResponse> verifyEmail(UUID emailId, UUID userId, String token, VerifyEmailRequest request);

    APIResource<DeleteEmailResponse> deleteEmail(UUID userId, UUID emailId, DeleteEmailRequest request);

    APIResource<SetPrimaryEmailResponse> setPrimaryEmail(UUID userId, UUID emailId);

    APIResource<Void> resendVerificationEmail(UUID userId, UUID emailId);

    void validateEmailNotUsed(String email);
}
