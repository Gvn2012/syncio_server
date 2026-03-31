package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewPhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdatePhoneRequest;
import io.github.gvn2012.user_service.dtos.requests.VerifyPhoneRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserPhone;
import io.github.gvn2012.user_service.entities.enums.PhoneStatus;
import io.github.gvn2012.user_service.entities.enums.PhoneType;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserPhoneRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.ITokenService;
import io.github.gvn2012.user_service.services.interfaces.IUserPhoneService;
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
public class UserPhoneServiceImpl implements IUserPhoneService {

        private static final int VERIFICATION_CODE_TTL_MINUTES = 10;
        private static final int VERIFICATION_CODE_LENGTH = 6;

        private final UserPhoneRepository userPhoneRepository;
        private final UserRepository userRepository;
        private final ITokenService tokenService;

        // ─── GET ALL PHONES ─────────────────────────────────────────────

        @Override
        public APIResource<GetUserPhoneResponse> getUserPhone(String userId) {
                Set<PhoneDto> phones = userPhoneRepository
                                .findAllByUser_Id(UUID.fromString(userId))
                                .stream()
                                .filter(phone -> phone.getStatus() != PhoneStatus.REMOVED)
                                .map(phone -> new PhoneDto(
                                                phone.getId().toString(),
                                                phone.getCountryCode(),
                                                phone.getPhoneNumber(),
                                                phone.getVerified(),
                                                phone.getPrimary()))
                                .collect(Collectors.toSet());

                return APIResource.ok(
                                "Get user phone successfully",
                                new GetUserPhoneResponse(phones));
        }

        // ─── ADD NEW PHONE ──────────────────────────────────────────────

        @Override
        @Transactional
        public APIResource<AddNewPhoneResponse> addNewPhone(UUID userId, AddNewPhoneRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("User not found"));

                String normalizedNumber = normalizePhoneNumber(request.getPhoneNumber());
                String countryCode = request.getCountryCode() != null ? request.getCountryCode() : "+84";

                validatePhoneNotUsed(normalizedNumber, countryCode);

                PhoneType phoneType = parsePhoneType(request.getPhoneType());
                String rawCode = generateVerificationCode();
                String hashedCode = tokenService.hashToken(rawCode);

                UserPhone phone = new UserPhone();
                phone.setUser(user);
                phone.setPhoneNumber(normalizedNumber);
                phone.setCountryCode(countryCode);
                phone.setPhoneType(phoneType);
                phone.setPrimary(false);
                phone.setVerified(false);
                phone.setStatus(PhoneStatus.UNVERIFIED);
                phone.setVerificationCodeHash(hashedCode);
                phone.setVerificationCodeExpiresAt(
                                LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));

                userPhoneRepository.save(phone);

                logVerificationCode(phone, rawCode);

                return APIResource.ok(
                                "Phone added successfully. Verification code sent.",
                                new AddNewPhoneResponse(phone.getId().toString()),
                                HttpStatus.CREATED);
        }

        // ─── UPDATE PHONE ───────────────────────────────────────────────

        @Override
        @Transactional
        public APIResource<UpdatePhoneResponse> updatePhone(UUID userId, UUID phoneId, UpdatePhoneRequest request) {
                UserPhone phone = userPhoneRepository
                                .findByIdAndUser_IdAndStatusIn(phoneId, userId,
                                                List.of(PhoneStatus.ACTIVE, PhoneStatus.UNVERIFIED))
                                .orElseThrow(() -> new NotFoundException("Phone not found"));

                String oldNumber = phone.getCountryCode() + phone.getPhoneNumber();
                boolean numberChanged = false;

                if (request.getPhoneNumber() != null) {
                        String normalizedNumber = normalizePhoneNumber(request.getPhoneNumber());
                        String countryCode = request.getCountryCode() != null
                                        ? request.getCountryCode()
                                        : phone.getCountryCode();

                        if (!normalizedNumber.equals(phone.getPhoneNumber())
                                        || !countryCode.equals(phone.getCountryCode())) {
                                validatePhoneNotUsed(normalizedNumber, countryCode);
                                phone.setPhoneNumber(normalizedNumber);
                                phone.setCountryCode(countryCode);
                                numberChanged = true;
                        }
                } else if (request.getCountryCode() != null
                                && !request.getCountryCode().equals(phone.getCountryCode())) {
                        validatePhoneNotUsed(phone.getPhoneNumber(), request.getCountryCode());
                        phone.setCountryCode(request.getCountryCode());
                        numberChanged = true;
                }

                if (request.getPhoneType() != null) {
                        phone.setPhoneType(parsePhoneType(request.getPhoneType()));
                }

                // If the number changed, reset verification
                if (numberChanged) {
                        phone.setVerified(false);
                        phone.setVerifiedAt(null);
                        phone.setStatus(PhoneStatus.UNVERIFIED);

                        String rawCode = generateVerificationCode();
                        phone.setVerificationCodeHash(tokenService.hashToken(rawCode));
                        phone.setVerificationCodeExpiresAt(
                                        LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));

                        logVerificationCode(phone, rawCode);
                }

                userPhoneRepository.save(phone);

                String newNumber = phone.getCountryCode() + phone.getPhoneNumber();

                UpdatePhoneResponse response = new UpdatePhoneResponse();
                response.setPhoneId(phone.getId().toString());
                response.setOldPhoneNumber(oldNumber);
                response.setNewPhoneNumber(newNumber);

                String message = numberChanged
                                ? "Phone updated. Verification code sent to the new number."
                                : "Phone updated successfully.";

                return APIResource.ok(message, response);
        }

        // ─── VERIFY PHONE ───────────────────────────────────────────────

        @Override
        @Transactional
        public APIResource<VerifyPhoneResponse> verifyPhone(UUID userId, UUID phoneId, VerifyPhoneRequest request) {
                UserPhone phone = userPhoneRepository
                                .findByIdAndUser_IdAndStatus(phoneId, userId, PhoneStatus.UNVERIFIED)
                                .orElseThrow(() -> new NotFoundException("Phone not found or already verified"));

                if (Boolean.TRUE.equals(phone.getVerified())) {
                        throw new BadRequestException("Phone already verified");
                }

                if (phone.getVerificationCodeHash() == null) {
                        throw new BadRequestException("No verification in progress. Request a new code.");
                }

                if (phone.getVerificationCodeExpiresAt() != null
                                && phone.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                        throw new BadRequestException("Verification code expired. Request a new one.");
                }

                if (!tokenService.matches(request.getCode(), phone.getVerificationCodeHash())) {
                        throw new BadRequestException("Invalid verification code");
                }

                phone.setVerified(true);
                phone.setVerifiedAt(LocalDateTime.now());
                phone.setStatus(PhoneStatus.ACTIVE);
                phone.setVerificationCodeHash(null);
                phone.setVerificationCodeExpiresAt(null);

                userPhoneRepository.save(phone);

                return APIResource.ok(
                                "Phone verified successfully",
                                new VerifyPhoneResponse(phone.getId().toString(), true));
        }

        // ─── DELETE PHONE ───────────────────────────────────────────────

        @Override
        @Transactional
        public APIResource<DeletePhoneResponse> deletePhone(UUID userId, UUID phoneId) {
                UserPhone phone = userPhoneRepository
                                .findByIdAndUser_IdAndStatusIn(phoneId, userId,
                                                List.of(PhoneStatus.ACTIVE, PhoneStatus.UNVERIFIED))
                                .orElseThrow(() -> new NotFoundException("Phone not found"));

                if (Boolean.TRUE.equals(phone.getPrimary())) {
                        long otherActiveCount = userPhoneRepository.countByUser_IdAndStatusNot(userId,
                                        PhoneStatus.REMOVED) - 1;
                        if (otherActiveCount <= 0) {
                                throw new BadRequestException(
                                                "Cannot remove your only phone number. Add another phone first.");
                        }
                }

                phone.setStatus(PhoneStatus.REMOVED);
                phone.setPrimary(false);
                phone.setVerified(false);
                phone.setVerifiedAt(null);
                phone.setVerificationCodeHash(null);
                phone.setVerificationCodeExpiresAt(null);

                userPhoneRepository.save(phone);

                return APIResource.ok(
                                "Phone removed successfully",
                                new DeletePhoneResponse(phone.getId().toString()));
        }

        @Override
        @Transactional
        public APIResource<SetPrimaryPhoneResponse> setPrimaryPhone(UUID userId, UUID phoneId) {
                UserPhone phone = userPhoneRepository
                                .findByIdAndUser_IdAndStatus(phoneId, userId, PhoneStatus.ACTIVE)
                                .orElseThrow(() -> new NotFoundException("Phone not found or not verified"));

                if (!Boolean.TRUE.equals(phone.getVerified())) {
                        throw new BadRequestException("Only verified phones can be set as primary");
                }

                if (Boolean.TRUE.equals(phone.getPrimary())) {
                        throw new BadRequestException("This phone is already the primary phone");
                }

                // Unset current primary if exists
                String previousPrimaryId = null;
                var currentPrimary = userPhoneRepository
                                .findByUser_IdAndPrimaryTrueAndStatusNot(userId, PhoneStatus.REMOVED);
                if (currentPrimary.isPresent()) {
                        previousPrimaryId = currentPrimary.get().getId().toString();
                        currentPrimary.get().setPrimary(false);
                        userPhoneRepository.save(currentPrimary.get());
                }

                phone.setPrimary(true);
                userPhoneRepository.save(phone);

                return APIResource.ok(
                                "Primary phone updated successfully",
                                new SetPrimaryPhoneResponse(phone.getId().toString(), previousPrimaryId));
        }

        @Override
        @Transactional
        public APIResource<Void> resendVerificationCode(UUID userId, UUID phoneId) {
                UserPhone phone = userPhoneRepository
                                .findByIdAndUser_IdAndStatus(phoneId, userId, PhoneStatus.UNVERIFIED)
                                .orElseThrow(() -> new NotFoundException("Phone not found or already verified"));

                String rawCode = generateVerificationCode();
                phone.setVerificationCodeHash(tokenService.hashToken(rawCode));
                phone.setVerificationCodeExpiresAt(
                                LocalDateTime.now().plusMinutes(VERIFICATION_CODE_TTL_MINUTES));

                userPhoneRepository.save(phone);

                logVerificationCode(phone, rawCode);

                return APIResource.ok(
                                "Verification code resent successfully",
                                null);
        }

        // ─── HELPERS ────────────────────────────────────────────────────

        private void validatePhoneNotUsed(String phoneNumber, String countryCode) {
                boolean exists = userPhoneRepository.existsByPhoneNumberAndCountryCodeAndStatusNot(
                                phoneNumber, countryCode, PhoneStatus.REMOVED);
                if (exists) {
                        throw new BadRequestException("Phone number already in use");
                }
        }

        private String normalizePhoneNumber(String phoneNumber) {
                if (phoneNumber == null) {
                        throw new BadRequestException("Phone number is required");
                }
                return phoneNumber.trim().replaceAll("[^0-9]", "");
        }

        private PhoneType parsePhoneType(String phoneType) {
                if (phoneType == null || phoneType.isBlank()) {
                        return PhoneType.MOBILE;
                }
                try {
                        return PhoneType.valueOf(phoneType.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new BadRequestException(
                                        "Invalid phone type. Valid types: MOBILE, WORK, HOME, FAX, OTHER");
                }
        }

        private String generateVerificationCode() {
                // Generate a numeric code of VERIFICATION_CODE_LENGTH digits
                StringBuilder code = new StringBuilder();
                java.security.SecureRandom random = new java.security.SecureRandom();
                for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
                        code.append(random.nextInt(10));
                }
                return code.toString();
        }

        private void logVerificationCode(UserPhone phone, String rawCode) {
                System.out.printf(
                                "[PHONE VERIFICATION] User=%s Phone=%s%s Code=%s (expires in %d min)%n",
                                phone.getUser().getId(),
                                phone.getCountryCode(),
                                phone.getPhoneNumber(),
                                rawCode,
                                VERIFICATION_CODE_TTL_MINUTES);
        }
}
