package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.responses.GetUserPhoneResponse;

import java.util.Set;

public interface IUserPhoneService {

    APIResource<GetUserPhoneResponse> getUserPhone (String userId);
}
