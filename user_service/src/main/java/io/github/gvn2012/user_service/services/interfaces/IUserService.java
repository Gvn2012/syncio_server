package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;

public interface IUserService {

    APIResource<LoginResponse> login(LoginRequest request);
    APIResource<UserRegisterResponse> register(UserRegisterRequest request);
    APIResource<GetUserDetailResponse> getUserDetail(String userId);
}
