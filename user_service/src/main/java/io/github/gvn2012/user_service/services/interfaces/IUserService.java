package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.CheckAvailableEmailAndUsernameWhenRegisterResponse;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IUserService {

    APIResource<LoginResponse> login(LoginRequest request);

    APIResource<UserRegisterResponse> register(UserRegisterRequest request);

    APIResource<GetUserDetailResponse> getUserDetail(String userId);

    APIResource<Map<UUID, GetUserDetailResponse>> getUsersDetail(Set<UUID> userIds);

    APIResource<CheckAvailableEmailAndUsernameWhenRegisterResponse> checkAvailableEmailAndUsernameWhenRegister(
            String email, String username);
}
