package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.DeleteEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.AddNewEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.DeleteEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.UpdateEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.VerifyEmailResponse;

import java.util.UUID;

public interface IUserEmailService {

    APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request);

    APIResource<UpdateEmailResponse> updateEmail(UUID userId, UUID emailId, UpdateEmailRequest request);

    APIResource<VerifyEmailResponse> verifyEmail(UUID emailId, UUID userId, String token, VerifyEmailRequest request);

    APIResource<DeleteEmailResponse> deleteEmail(UUID userId, UUID emailId, DeleteEmailRequest request);
}
