package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.shared.kafka_events.EmailVerificationEvent;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.DeleteEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserEmailRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.ITokenService;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import io.github.gvn2012.user_service.services.kafka.EmailEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEmailServiceImpl implements IUserEmailService {

    private static final int VERIFICATION_TOKEN_TTL_MINUTES = 15;

    private final ITokenService tokenService;
    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;
    private final EmailEventProducer emailEventProducer;

    private record PendingEmail(UserEmail email, String rawToken) {
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
                        email.getPrimary()
                ))
                .collect(Collectors.toSet());

        return APIResource.ok(
                "Get user emails successfully",
                new GetUserEmailResponse(emails)
        );
    }

    @Override
    @Transactional
    public APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateEmailNotUsed(request.getEmail());

        PendingEmail pending = createPendingEmail(user, request.getEmail());
        userEmailRepository.save(pending.email());

        sendVerificationEmail(pending.email(), pending.rawToken());

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

        PendingEmail pending = createPendingEmail(currentUser, newEmail);
        userEmailRepository.save(pending.email());

        sendVerificationEmail(pending.email(), pending.rawToken());

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
            VerifyEmailRequest request) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.UNVERIFIED)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if (Boolean.TRUE.equals(email.getVerified())) {
            throw new BadRequestException("Email already verified");
        }

        if (email.getVerificationTokenHash() == null) {
            throw new BadRequestException("No verification in progress");
        }

        if (email.getVerificationTokenHashExpiresAt() != null
                && email.getVerificationTokenHashExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expired");
        }

        if (!tokenService.matches(token, email.getVerificationTokenHash())) {
            throw new BadRequestException("Invalid token");
        }

        email.setVerified(true);
        email.setVerifiedAt(LocalDateTime.now());
        email.setStatus(EmailStatus.ACTIVE);
        email.setVerificationTokenHash(null);
        email.setVerificationTokenHashExpiresAt(null);

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
        email.setVerificationTokenHash(null);
        email.setVerificationTokenHashExpiresAt(null);

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
                HttpStatus.OK
        );
    }

    @Override
    @Transactional
    public APIResource<Void> resendVerificationEmail(UUID userId, UUID emailId) {
        UserEmail email = userEmailRepository
                .findByIdAndUser_IdAndStatus(emailId, userId, EmailStatus.UNVERIFIED)
                .orElseThrow(() -> new NotFoundException("Email not found"));

        String rawToken = tokenService.generateToken();
        String hashedToken = tokenService.hashToken(rawToken);

        email.setVerificationTokenHash(hashedToken);
        email.setVerificationTokenHashExpiresAt(
                LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_TTL_MINUTES));

        userEmailRepository.save(email);

        sendVerificationEmail(email, rawToken);

        return APIResource.ok(
                "Verification email resent successfully",
                null,
                HttpStatus.OK);
    }

    private PendingEmail createPendingEmail(User user, String email) {
        String normalizedEmail = normalizeEmail(email);
        String rawToken = tokenService.generateToken();
        String hashedToken = tokenService.hashToken(rawToken);

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(user);
        userEmail.setEmail(normalizedEmail);
        userEmail.setPrimary(false);
        userEmail.setVerified(false);
        userEmail.setVerifiedAt(null);
        userEmail.setStatus(EmailStatus.UNVERIFIED);
        userEmail.setVerificationTokenHash(hashedToken);
        userEmail.setVerificationTokenHashExpiresAt(
                LocalDateTime.now().plusMinutes(VERIFICATION_TOKEN_TTL_MINUTES));

        return new PendingEmail(userEmail, rawToken);
    }

    private void sendVerificationEmail(UserEmail email, String rawToken) {

        String link = buildVerificationLink(
                email.getUser().getId(),
                email.getId(),
                rawToken);

        EmailVerificationEvent event = new EmailVerificationEvent();
        event.setUserId(email.getUser().getId());
        event.setEmailId(email.getId());
        event.setEmail(email.getEmail());
        event.setVerificationLink(link);

        emailEventProducer.send(event);
    }

    private String buildVerificationLink(UUID userId, UUID emailId, String rawToken) {
        return "http://localhost:8080/api/v1/users/emails/verify"
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
}