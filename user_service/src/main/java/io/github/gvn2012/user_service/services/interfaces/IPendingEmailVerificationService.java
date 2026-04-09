package io.github.gvn2012.user_service.services.interfaces;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.StartEmailVerificationRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPendingEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.StartEmailVerificationResponse;
import io.github.gvn2012.user_service.dtos.responses.VerifyPendingEmailResponse;
import io.github.gvn2012.user_service.entities.PendingEmailVerification;

import java.util.UUID;

public interface IPendingEmailVerificationService {

    APIResource<StartEmailVerificationResponse> start(StartEmailVerificationRequest request);

    APIResource<VerifyPendingEmailResponse> verify(UUID verificationId, VerifyPendingEmailRequest request);

    APIResource<Void> resend(UUID verificationId);

    PendingEmailVerification requireVerifiedForRegistration(UUID verificationId, String email);

    void markConsumed(PendingEmailVerification verification);

}
