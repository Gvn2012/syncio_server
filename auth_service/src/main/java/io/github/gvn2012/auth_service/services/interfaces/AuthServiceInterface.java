package io.github.gvn2012.auth_service.services.interfaces;

import io.github.gvn2012.auth_service.dtos.APIResource;
import io.github.gvn2012.auth_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.auth_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.auth_service.dtos.responses.ValidateResponse;

public interface AuthServiceInterface {

    APIResource<GenerateLoginTokenResponse> generateLoginToken(GenerateLoginTokenRequest request);

    ValidateResponse validateToken(String token);
}
