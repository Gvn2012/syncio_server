package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.AddNewEmailResponse;

import java.util.UUID;

public interface IUserEmailService {

    APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request);
}
