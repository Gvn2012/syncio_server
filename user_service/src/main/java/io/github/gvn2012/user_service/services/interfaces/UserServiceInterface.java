package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;

public interface UserServiceInterface {

    APIResource<LoginResponse> login(LoginRequest request);
}
