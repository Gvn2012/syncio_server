package io.github.gvn2012.user_service.services.impls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gvn2012.user_service.utils.RequestMetadataUtils;
import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.DeleteEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.entities.enums.EmailVerificationMethod;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserEmailRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.ITokenService;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import io.github.gvn2012.user_service.services.kafka.EmailEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailServiceImpl implements IUserEmailService {

    private static final int VERIFICATION_TOKEN_TTL_MINUTES = 15;
    private static final int VERIFICATION_CODE_TTL_MINUTES = 10;
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final String BASE_URL = "http://syncio.site";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ITokenService tokenService;
    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;
    private final EmailEventProducer emailEventProducer;
    private final ObjectMapper objectMapper;

    private record PendingEmail(UserEmail email, String rawSecret) {
    }

    @Override
    public APIResource<GetUserEmailResponse> getUserEmail(String userId) {
        Set<EmailDto> emails = userEmailRepository
                .findAllByUser_Id(UUID.fromString(userId))
                .stream()
                .filter(email -> email.getStatus() != EmailStatus.REMOVED)
                .map(email -> new EmailDto(
                        email.getId().toString(),
                        email.getEmail(),
                        email.getVerified(),
                        email.getPrimary()))
                .collect(Collectors.toSet());

        return APIResource.ok(
                "Get user emails successfully",
                new GetUserEmailResponse(emails));
    }

    @Override
    @Transactional
    public APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateEmailNotUsed(request.getEmail());

        PendingEmail pending = createPendingEmail(user, request.getEmail(), false, EmailVerificationMethod.TOKEN);

        java.util.Map<String, Object> finalMetadata = RequestMetadataUtils.extractMetadata();

        if (!finalMetadata.isEmpty()) {
            try {
                pending.email().setMetadata(objectMapper.writeValueAsString(finalMetadata));
            } catch (Exception e) {
                log.error("Failed to serialize metadata", e);
            }
        }

        userEmailRepository.save(pending.email());

        sendVerificationEmail(pending.email(), pending.rawSecret());

        return APIResource.ok(
                "Added email successfully. Verification email sent.",
                new AddNewEmailResponse(pending.email().getId().toString()),
                HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public APIResource<UpdateEmailResponse> updateEmail(UUID userId, UUID emailId, UpdateEmailRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserEmail currentEmail = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String newEmail = normalizeEmail(request.getEmail());

        if (currentEmail.getEmail().equalsIgnoreCase(newEmail)) {
            throw new BadRequestException("New email is the same as current email");
        }

        validateEmailNotUsed(newEmail);

        PendingEmail pending = createPendingEmail(currentUser, newEmail, false, EmailVerificationMethod.TOKEN);

        java.util.Map<String, Object> finalMetadata = RequestMetadataUtils.extractMetadata();

        if (!finalMetadata.isEmpty()) {
            try {
                pending.email().setMetadata(objectMapper.writeValueAsString(finalMetadata));
            } catch (Exception e) {
                log.error("Failed to serialize metadata", e);
            }
        }

        userEmailRepository.save(pending.email());

        sendVerificationEmail(pending.email(), pending.rawSecret());

        UpdateEmailResponse response = new UpdateEmailResponse();
        response.setOldEmail(currentEmail.getEmail());
        response.setNewEmail(newEmail);
        response.setNewEmailId(pending.email().getId().toString());

        return APIResource.ok(
                "Update requested successfully. Verification email sent to the new address.",
                response,
                HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<VerifyEmailResponse> verifyEmail(
            UUID emailId,
            UUID userId,
            String token,
            String code) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.UNVERIFIED)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if (Boolean.TRUE.equals(email.getVerified())) {
            throw new BadRequestException("Email already verified");
        }

        EmailVerificationMethod verificationMethod = resolveVerificationMethod(email);
        if (verificationMethod == EmailVerificationMethod.OTP) {
            verifyByCode(email, code);
        } else {
            verifyByToken(email, token);
        }

        email.setVerified(true);
        email.setVerifiedAt(LocalDateTime.now());
        email.setStatus(EmailStatus.ACTIVE);
        email.setVerificationMethod(null);
        email.setVerificationTokenHash(null);
        email.setVerificationTokenHashExpiresAt(null);
        email.setVerificationCodeHash(null);
        email.setVerificationCodeHashExpiresAt(null);

        java.util.Map<String, Object> requestMetadata = RequestMetadataUtils.extractMetadata();
        if (!requestMetadata.isEmpty()) {
            try {
                Map<String, Object> existingMetadata = new HashMap<>();
                if (email.getMetadata() != null && !email.getMetadata().isBlank()) {
                    existingMetadata = objectMapper.readValue(email.getMetadata(),
                            new TypeReference<Map<String, Object>>() {
                            });
                }
                existingMetadata.putAll(requestMetadata);

                email.setMetadata(objectMapper.writeValueAsString(existingMetadata));
            } catch (Exception e) {
                log.error("Failed to update metadata", e);
            }
        }

        userEmailRepository.save(email);

        return APIResource.ok(
                "Email verified successfully",
                new VerifyEmailResponse(),
                HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<DeleteEmailResponse> deleteEmail(UUID userId, UUID emailId, DeleteEmailRequest request) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatusIn(
                        emailId,
                        userId,
                        List.of(EmailStatus.ACTIVE, EmailStatus.UNVERIFIED))
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if (Boolean.TRUE.equals(email.getPrimary())) {
            long otherActiveCount = userEmailRepository.countByUser_IdAndStatusNot(userId, EmailStatus.REMOVED) - 1;
            if (otherActiveCount <= 0) {
                throw new BadRequestException("Cannot remove your only email. Add another email first.");
            }
        }

        email.setStatus(EmailStatus.REMOVED);
        email.setVerified(false);
        email.setPrimary(false);
        email.setVerifiedAt(null);
        email.setVerificationMethod(null);
        email.setVerificationTokenHash(null);
        email.setVerificationTokenHashExpiresAt(null);
        email.setVerificationCodeHash(null);
        email.setVerificationCodeHashExpiresAt(null);

        userEmailRepository.save(email);

        return APIResource.ok(
                "Email removed successfully",
                new DeleteEmailResponse(),
                HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<SetPrimaryEmailResponse> setPrimaryEmail(UUID userId, UUID emailId) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Email not found or not verified"));

        if (!Boolean.TRUE.equals(email.getVerified())) {
            throw new BadRequestException("Only verified emails can be set as primary");
        }

        if (Boolean.TRUE.equals(email.getPrimary())) {
            throw new BadRequestException("This email is already the primary email");
        }

        String previousPrimaryId = null;
        var currentPrimary = userEmailRepository
                .findByUser_IdAndPrimaryTrueAndStatusNot(userId, EmailStatus.REMOVED);
        if (currentPrimary.isPresent()) {
            previousPrimaryId = currentPrimary.get().getId().toString();
            currentPrimary.get().setPrimary(false);
            userEmailRepository.save(currentPrimary.get());
        }

        email.setPrimary(true);
        userEmailRepository.save(email);

        return APIResource.ok(
                "Primary email updated successfully",
                new SetPrimaryEmailResponse(email.getId().toString(), previousPrimaryId),
                HttpStatus.OK);
    }

    @Override
    @Transactional
    public APIResource<Void> resendVerificationEmail(UUID userId, UUID emailId) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.UNVERIFIED)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        PendingEmail pending = refreshVerification(email);

        userEmailRepository.save(email);

        sendVerificationEmail(email, pending.rawSecret());

        return APIResource.ok(
                "Verification email resent successfully",
                null,
                HttpStatus.OK);
    }

    private PendingEmail createPendingEmail(
            User user,
            String email,
            boolean primary,
            EmailVerificationMethod verificationMethod) {
        String normalizedEmail = normalizeEmail(email);

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(user);
        userEmail.setEmail(normalizedEmail);
        userEmail.setPrimary(primary);
        userEmail.setVerified(false);
        userEmail.setVerifiedAt(null);
        userEmail.setStatus(EmailStatus.UNVERIFIED);
        userEmail.setVerificationMethod(verificationMethod);

        return refreshVerification(userEmail);
    }

    public UserEmail createRegistrationEmail(User user, String email) {
        return createPendingEmail(user, email, true, EmailVerificationMethod.OTP).email();
    }

    public void sendRegistrationVerification(UserEmail email) {
        PendingEmail pending = refreshVerification(email);
        userEmailRepository.save(email);
        sendVerificationEmail(email, pending.rawSecret());
    }

    private PendingEmail refreshVerification(UserEmail email) {
        EmailVerificationMethod verificationMethod = resolveVerificationMethod(email);
        if (verificationMethod == EmailVerificationMethod.OTP) {
            String rawCode = generateVerificationCode();
            email.setVerificationMethod(EmailVerificationMethod.OTP);
            email.setVerificationCodeHash(tokenService.hashToken(rawCode));
            email.setVerificationCodeHashExpiresAt(
                    LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));
            email.setVerificationTokenHash(null);
            email.setVerificationTokenHashExpiresAt(null);
            return new PendingEmail(email, rawCode);
        }

        String rawToken = tokenService.generateToken();
        email.setVerificationMethod(EmailVerificationMethod.TOKEN);
        email.setVerificationTokenHash(tokenService.hashToken(rawToken));
        email.setVerificationTokenHashExpiresAt(
                LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_TTL_MINUTES));
        email.setVerificationCodeHash(null);
        email.setVerificationCodeHashExpiresAt(null);

        return new PendingEmail(email, rawToken);
    }

    private void verifyByToken(UserEmail email, String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token is required");
        }
        if (email.getVerificationTokenHash() == null) {
            throw new BadRequestException("No token verification in progress");
        }
        if (email.getVerificationTokenHashExpiresAt() != null
                && email.getVerificationTokenHashExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }
        if (!tokenService.matches(token, email.getVerificationTokenHash())) {
            throw new BadRequestException("Invalid token");
        }
    }

    private void verifyByCode(UserEmail email, String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Verification code is required");
        }
        if (email.getVerificationCodeHash() == null) {
            throw new BadRequestException("No code verification in progress");
        }
        if (email.getVerificationCodeHashExpiresAt() != null
                && email.getVerificationCodeHashExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code expired");
        }
        if (!tokenService.matches(code, email.getVerificationCodeHash())) {
            throw new BadRequestException("Invalid verification code");
        }
    }

    private EmailVerificationMethod resolveVerificationMethod(UserEmail email) {
        if (email.getVerificationMethod() != null) {
            return email.getVerificationMethod();
        }
        if (email.getVerificationCodeHash() != null) {
            return EmailVerificationMethod.OTP;
        }
        return EmailVerificationMethod.TOKEN;
    }

    private String generateVerificationCode() {
        StringBuilder builder = new StringBuilder(VERIFICATION_CODE_LENGTH);
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    private void sendVerificationEmail(UserEmail email, String rawSecret) {

        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setUserId(email.getUser().getId());
        event.setEmailId(email.getId());
        event.setEmail(email.getEmail());
        if (resolveVerificationMethod(email) == EmailVerificationMethod.OTP) {
            event.setVerificationCode(rawSecret);
            event.setVerificationLink(null);
        } else {
            event.setVerificationLink(buildVerificationLink(
                    email.getUser().getId(),
                    email.getId(),
                    rawSecret));
            event.setVerificationCode(null);
        }

        emailEventProducer.send(event);
    }

    private String buildVerificationLink(UUID userId, UUID emailId, String rawToken) {
        return BASE_URL + "/api/v1/users/emails/verify"
                + "?emailId=" + emailId
                + "&userId=" + userId
                + "&token=" + rawToken;
    }

    public void validateEmailNotUsed(String email) {
        String normalizedEmail = normalizeEmail(email);

        boolean exists = userEmailRepository.existsByEmailAndStatusNot(
                normalizedEmail,
                EmailStatus.REMOVED);

        if (exists) {
            throw new BadRequestException("Email already existed");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new BadRequestException("Email is required");
        }
        return email.trim().toLowerCase();
    }

    public Boolean isEmailAvailable(String email) {
        String normalizedEmail = normalizeEmail(email);
        return !userEmailRepository.existsByEmailAndStatusNot(
                normalizedEmail,
                EmailStatus.REMOVED);
    }
}
