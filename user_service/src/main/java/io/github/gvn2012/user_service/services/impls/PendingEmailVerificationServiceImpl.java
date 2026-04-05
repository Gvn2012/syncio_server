package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.StartEmailVerificationRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPendingEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.StartEmailVerificationResponse;
import io.github.gvn2012.user_service.dtos.responses.VerifyPendingEmailResponse;
import io.github.gvn2012.user_service.entities.PendingEmailVerification;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.PendingEmailVerificationRepository;
import io.github.gvn2012.user_service.services.interfaces.ITokenService;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import io.github.gvn2012.user_service.services.interfaces.IPendingEmailVerificationService;
import io.github.gvn2012.user_service.services.kafka.EmailEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PendingEmailVerificationServiceImpl implements IPendingEmailVerificationService {

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_TTL_MINUTES = 10;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int REGISTRATION_TTL_HOURS = 24;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PendingEmailVerificationRepository pendingEmailVerificationRepository;
    private final IUserEmailService userEmailService;
    private final ITokenService tokenService;
    private final EmailEventProducer emailEventProducer;

    @Override
    @Transactional
    public APIResource<StartEmailVerificationResponse> start(StartEmailVerificationRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        // userEmailService.validateEmailNotUsed(normalizedEmail);

        String rawCode = generateVerificationCode();

        PendingEmailVerification verification = new PendingEmailVerification();
        verification.setEmail(normalizedEmail);
        verification.setVerified(false);
        verification.setVerifiedAt(null);
        verification.setVerificationCodeHash(tokenService.hashToken(rawCode));
        verification.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));
        verification.setResendAvailableAt(LocalDateTime.now().plusSeconds(RESEND_COOLDOWN_SECONDS));
        verification.setRegistrationExpiresAt(null);
        verification.setConsumedAt(null);

        pendingEmailVerificationRepository.save(verification);
        log.info("Saved to DB");
        sendOtpEmail(verification, rawCode);

        return APIResource.ok(
                "Verification code sent successfully",
                new StartEmailVerificationResponse(
                        verification.getId().toString(),
                        verification.getEmail(),
                        RESEND_COOLDOWN_SECONDS),
                HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public APIResource<VerifyPendingEmailResponse> verify(UUID verificationId, VerifyPendingEmailRequest request) {
        PendingEmailVerification verification = getActiveVerification(verificationId);

        if (Boolean.TRUE.equals(verification.getVerified())) {
            return APIResource.ok(
                    "Email already verified",
                    new VerifyPendingEmailResponse(
                            verification.getId().toString(),
                            verification.getEmail(),
                            true));
        }

        if (verification.getVerificationCodeHash() == null) {
            throw new BadRequestException("No verification code in progress");
        }

        if (verification.getVerificationCodeExpiresAt() != null
                && verification.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code expired");
        }

        if (!tokenService.matches(request.getCode(), verification.getVerificationCodeHash())) {
            throw new BadRequestException("Invalid verification code");
        }

        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerificationCodeHash(null);
        verification.setVerificationCodeExpiresAt(null);
        verification.setRegistrationExpiresAt(LocalDateTime.now().plusHours(REGISTRATION_TTL_HOURS));
        pendingEmailVerificationRepository.save(verification);

        return APIResource.ok(
                "Email verified successfully",
                new VerifyPendingEmailResponse(
                        verification.getId().toString(),
                        verification.getEmail(),
                        true));
    }

    @Override
    @Transactional
    public APIResource<Void> resend(UUID verificationId) {
        PendingEmailVerification verification = getActiveVerification(verificationId);

        if (Boolean.TRUE.equals(verification.getVerified())) {
            throw new BadRequestException("Email already verified");
        }

        if (verification.getResendAvailableAt() != null
                && verification.getResendAvailableAt().isAfter(LocalDateTime.now())) {
            long seconds = Duration.between(LocalDateTime.now(), verification.getResendAvailableAt()).getSeconds();
            throw new BadRequestException("Please wait " + Math.max(1, seconds) + " seconds before resending");
        }

        String rawCode = generateVerificationCode();
        verification.setVerificationCodeHash(tokenService.hashToken(rawCode));
        verification.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));
        verification.setResendAvailableAt(LocalDateTime.now().plusSeconds(RESEND_COOLDOWN_SECONDS));
        pendingEmailVerificationRepository.save(verification);

        sendOtpEmail(verification, rawCode);

        return APIResource.ok("Verification code resent successfully", null, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public PendingEmailVerification requireVerifiedForRegistration(UUID verificationId, String email) {
        PendingEmailVerification verification = getActiveVerification(verificationId);
        String normalizedEmail = normalizeEmail(email);
        userEmailService.validateEmailNotUsed(normalizedEmail);

        if (!verification.getEmail().equals(normalizedEmail)) {
            throw new BadRequestException("Email verification does not match the email used for registration");
        }

        if (!Boolean.TRUE.equals(verification.getVerified())) {
            throw new BadRequestException("Email has not been verified");
        }

        if (verification.getRegistrationExpiresAt() != null
                && verification.getRegistrationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verified email expired. Request a new verification.");
        }

        return verification;
    }

    @Override
    @Transactional
    public void markConsumed(PendingEmailVerification verification) {
        verification.setConsumedAt(LocalDateTime.now());
        pendingEmailVerificationRepository.save(verification);
    }

    private PendingEmailVerification getActiveVerification(UUID verificationId) {
        PendingEmailVerification verification = pendingEmailVerificationRepository
                .findByIdAndConsumedAtIsNull(verificationId)
                .orElseThrow(() -> new NotFoundException("Email verification not found"));

        if (verification.getRegistrationExpiresAt() != null
                && verification.getRegistrationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Email verification expired");
        }

        return verification;
    }

    private void sendOtpEmail(PendingEmailVerification verification, String rawCode) {
        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setEmail(verification.getEmail());
        event.setEmailId(verification.getId());
        event.setVerificationCode(rawCode);
        event.setVerificationLink(null);
        emailEventProducer.send(event);
        log.info("Kafka produce {}", event);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new BadRequestException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    private String generateVerificationCode() {
        StringBuilder builder = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
