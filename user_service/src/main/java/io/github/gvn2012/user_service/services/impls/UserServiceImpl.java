package io.github.gvn2012.user_service.services.impls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import io.github.gvn2012.user_service.clients.AuthClient;
import io.github.gvn2012.user_service.clients.OrgClient;
import io.github.gvn2012.user_service.clients.PermissionClient;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.OrgRegisterDTO;
import io.github.gvn2012.user_service.dtos.UserAddressDTO;
import io.github.gvn2012.user_service.dtos.UserEmergencyContactDTO;
import io.github.gvn2012.user_service.dtos.mappers.UserDetailMapper;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.*;
import io.github.gvn2012.user_service.entities.PendingEmailVerification;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserAddress;
import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.UserEmergencyContact;
import io.github.gvn2012.user_service.entities.UserPhone;
import io.github.gvn2012.user_service.entities.UserProfile;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import io.github.gvn2012.user_service.entities.enums.AddressType;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.entities.enums.EmailVerificationMethod;
import io.github.gvn2012.user_service.entities.enums.Gender;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.DataIntegrityViolationException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserPhoneRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IPendingEmailVerificationService;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import io.github.gvn2012.user_service.utils.RequestMetadataUtils;
import io.github.gvn2012.shared.kafka_events.UserSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserPhoneRepository userPhoneRepository;

    private final AuthClient authClient;
    private final OrgClient orgClient;
    private final PermissionClient permissionClient;

    private final IUserEmailService userEmailService;
    private final IPendingEmailVerificationService pendingEmailVerificationService;

    private final UserDetailMapper userDetailMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final io.github.gvn2012.user_service.clients.UploadClient uploadClient;

    @Override
    @Transactional(readOnly = true)
    public APIResource<LoginResponse> login(LoginRequest loginRequest) {
        User user = userRepository.getByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        ensureUserCanLogin(user);

        if (loginRequest.getOrgId() != null && !loginRequest.getOrgId().isBlank()) {
            if (user.getOrgId() == null) {
                throw new BadRequestException("User does not belong to any organization. Please use standalone login.");
            }
            if (!user.getOrgId().toString().equalsIgnoreCase(loginRequest.getOrgId())) {
                throw new BadRequestException("User does not belong to the requested organization.");
            }
        } else {
            if (user.getOrgId() != null) {
                throw new BadRequestException("User belongs to an organization. Please use organization login.");
            }
        }

        GenerateLoginTokenResponse tokenResponse = null;

        try {
            tokenResponse = authClient.generateToken(
                    new GenerateLoginTokenRequest(
                            user.getUsername(),
                            user.getId().toString()))
                    .block(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.error("Failed to generate token: {}", e.getMessage());
            throw new BadRequestException("Failed to generate token: " + e.getMessage());
        }

        if (tokenResponse == null) {
            throw new BadRequestException("Failed to generate token");
        }

        LoginResponse response = new LoginResponse(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                user.getId().toString(),
                tokenResponse.getUserRoles(),
                user.getOrgId() != null ? user.getOrgId().toString() : null);

        return APIResource.ok("Login successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<GetUserDetailResponse> getUserDetail(String userId) {

        User user = userRepository.findDetailById(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        ensureUserAccessible(user);

        GetUserDetailResponse response = userDetailMapper.toDto(user);
        enrichUserMediaUrls(Collections.singletonList(response));
        return APIResource.ok("Get user detail successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<Map<UUID, GetUserDetailResponse>> getUsersDetail(java.util.Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return APIResource.ok("No users requested", new HashMap<>());
        }

        List<User> users = userRepository.findDetailsByIdIn(userIds);
        Map<UUID, GetUserDetailResponse> responseMap = new HashMap<>();

        for (User user : users) {
            try {
                ensureUserAccessible(user);
                responseMap.put(user.getId(), userDetailMapper.toDto(user));
            } catch (Exception e) {
                log.warn("User {} is not accessible or failed to map, skipping", user.getId(), e);
            }
        }

        enrichUserMediaUrls(new java.util.ArrayList<>(responseMap.values()));

        return APIResource.ok("Batch user details retrieved successfully", responseMap);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<Map<UUID, UserSummaryResponse>> getUsersSummary(java.util.Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return APIResource.ok("No users requested", new HashMap<>());
        }

        List<User> users = userRepository.findSummariesByIdIn(userIds);
        Map<UUID, UserSummaryResponse> responseMap = new HashMap<>();

        Set<String> pathsToSign = new HashSet<>();
        for (User user : users) {
            String avatarUrl = null;
            String avatarPath = null;
            if (user.getProfile() != null) {
                UserProfilePicture primaryPic = user.getProfile().getPictures().stream()
                        .filter(p -> Boolean.TRUE.equals(p.getPrimary()) && !Boolean.TRUE.equals(p.getDeleted()))
                        .findFirst()
                        .orElse(null);

                if (primaryPic != null) {
                    avatarPath = primaryPic.getObjectPath();
                    avatarUrl = primaryPic.getUrl();
                    if (avatarPath != null) {
                        pathsToSign.add(avatarPath);
                    }
                }
            }

            responseMap.put(user.getId(), UserSummaryResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .displayName(user.getFirstName() + " " + user.getLastName())
                    .avatarUrl(avatarUrl)
                    .avatarPath(avatarPath)
                    .active(user.getActive())
                    .suspended(user.getSuspended())
                    .banned(user.getBanned())
                    .build());
        }

        if (!pathsToSign.isEmpty()) {
            io.github.gvn2012.user_service.dtos.responses.DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new io.github.gvn2012.user_service.dtos.requests.DownloadUrlRequestDTO(pathsToSign));
            Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();
            for (UserSummaryResponse summary : responseMap.values()) {
                if (summary.getAvatarPath() != null) {
                    summary.setAvatarUrl(signedUrls.getOrDefault(summary.getAvatarPath(), summary.getAvatarUrl()));
                }
            }
        }

        return APIResource.ok("Batch user summaries retrieved successfully", responseMap);
    }

    private void enrichUserMediaUrls(List<GetUserDetailResponse> responses) {
        if (responses == null || responses.isEmpty()) return;

        Set<String> pathsToSign = new HashSet<>();
        for (GetUserDetailResponse res : responses) {
            if (res.getUserProfileResponse() != null && res.getUserProfileResponse().getUserProfilePictureResponseList() != null) {
                res.getUserProfileResponse().getUserProfilePictureResponseList().stream()
                        .map(UserProfilePictureResponse::getObjectPath)
                        .filter(Objects::nonNull)
                        .forEach(pathsToSign::add);
            }
        }

        if (pathsToSign.isEmpty()) return;

        io.github.gvn2012.user_service.dtos.responses.DownloadUrlResponseDTO signedUrlsRes = uploadClient.getDownloadUrls(new io.github.gvn2012.user_service.dtos.requests.DownloadUrlRequestDTO(pathsToSign));
        Map<String, String> signedUrls = signedUrlsRes != null ? signedUrlsRes.getDownloadUrls() : Map.of();

        for (GetUserDetailResponse res : responses) {
            if (res.getUserProfileResponse() != null && res.getUserProfileResponse().getUserProfilePictureResponseList() != null) {
                for (UserProfilePictureResponse pic : res.getUserProfileResponse().getUserProfilePictureResponseList()) {
                    if (pic.getObjectPath() != null) {
                        pic.setUrl(signedUrls.getOrDefault(pic.getObjectPath(), pic.getUrl()));
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public APIResource<UserRegisterResponse> register(UserRegisterRequest request) {
        validateRegisterRequest(request);
        PendingEmailVerification verification = pendingEmailVerificationService
                .requireVerifiedForRegistration(request.getEmailVerificationId(), request.getEmail());

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = buildUserAggregate(request, hashedPassword, verification);

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Username, email, or phone already exists");
        }

        pendingEmailVerificationService.markConsumed(verification);

        if ("admin".equalsIgnoreCase(request.getRegistrationType()) && request.getOrganization() != null) {
            createOrganizationForAdmin(user.getId(), request.getOrganization(), request.getEmail());
        }

        permissionClient.initUserRole(user.getId().toString(),
                request.getRegistrationType()).block(Duration.ofSeconds(5));

        UserRegisterResponse response = new UserRegisterResponse(user.getId().toString());

        try {
            UserSearchEvent searchEvent = UserSearchEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .avatarUrl(user.getProfile().getPictures().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getPrimary()))
                            .findFirst()
                            .map(UserProfilePicture::getUrl)
                            .orElse(null))
                    .operationType(UserSearchEvent.OperationType.UPSERT)
                    .build();
            kafkaTemplate.send("user-search-indexing", String.valueOf(user.getId()), searchEvent);
        } catch (Exception e) {
            log.error("Failed to send user search indexing event", e);
        }

        return APIResource.ok(
                "User created successfully",
                response,
                HttpStatus.CREATED);
    }

    private void validateRegisterRequest(UserRegisterRequest request) {
        if (userRepository.existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse(request.getUsername())) {
            throw new BadRequestException("The username already exists");
        }

        if (userPhoneRepository.existsUserPhoneByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone already exists");
        }

        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
        if (!password.matches(passwordRegex)) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character");
        }
    }

    private void ensureUserAccessible(User user) {
        if (Boolean.TRUE.equals(user.getSoftDeleted())) {
            throw new BadRequestException("Account is soft deleted");
        }
        if (Boolean.TRUE.equals(user.getHardDeleted())) {
            throw new BadRequestException("Account is hard deleted");
        }
    }

    private void ensureUserCanLogin(User user) {
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("Account is not active");
        }
        if (Boolean.TRUE.equals(user.getSoftDeleted())) {
            throw new BadRequestException("Account is soft deleted");
        }
        if (Boolean.TRUE.equals(user.getHardDeleted())) {
            throw new BadRequestException("Account is hard deleted");
        }
        if (Boolean.TRUE.equals(user.getSuspended())) {
            throw new BadRequestException("Account is suspended");
        }
        if (Boolean.TRUE.equals(user.getBanned())) {
            throw new BadRequestException("Account is banned");
        }
    }

    private User buildUserAggregate(UserRegisterRequest request, String hashedPassword,
            PendingEmailVerification verification) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        if (request.getGender() != null) {
            user.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }

        Map<String, Object> requestMetadata = RequestMetadataUtils.extractMetadata();
        if (requestMetadata.get("timezone") != null) {
            user.setTimezone(requestMetadata.get("timezone").toString());
        }
        if (requestMetadata.get("locale") != null) {
            user.setLocale(requestMetadata.get("locale").toString());
        }

        UserEmail email = new UserEmail();
        email.setUser(user);
        email.setEmail(request.getEmail().trim().toLowerCase());
        email.setVerified(true);
        email.setVerifiedAt(LocalDateTime.now());
        email.setPrimary(true);
        email.setStatus(EmailStatus.ACTIVE);
        email.setVerificationMethod(EmailVerificationMethod.OTP);
        try {
            Map<String, Object> finalMetadata = new HashMap<>();
            if (verification.getMetadata() != null && !verification.getMetadata().isBlank()) {
                finalMetadata = objectMapper.readValue(verification.getMetadata(),
                        new TypeReference<Map<String, Object>>() {
                        });
            }

            finalMetadata.putAll(requestMetadata);

            email.setMetadata(objectMapper.writeValueAsString(finalMetadata));
        } catch (Exception e) {
            log.error("Failed to merge registration metadata", e);
        }

        UserPhone phone = new UserPhone();
        phone.setUser(user);
        phone.setPhoneNumber(request.getPhoneCode() + request.getPhoneNumber());
        phone.setPrimary(true);
        phone.setVerified(true);
        phone.setVerifiedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDateOfBirth(request.getDateBirth());

        UserProfilePicture picture = new UserProfilePicture();
        picture.setUserProfile(profile);
        picture.setPrimary(true);
        picture.setDeleted(false);

        if (request.getProfileImageId() != null) {
            picture.setExternalId(request.getProfileImageId().toString());
            picture.setUrl("PENDING");
        } else {
            picture.setUrl("https://storage.googleapis.com/syncio-bucket/defaults/avatar.png");
        }

        user.getEmails().add(email);
        user.getPhones().add(phone);
        user.setProfile(profile);
        profile.getPictures().add(picture);

        buildAddresses(user, request.getAddresses());
        buildEmergencyContacts(user, request.getEmergencyContacts());

        return user;
    }

    private void buildAddresses(User user, List<UserAddressDTO> dtos) {
        if (dtos == null || dtos.isEmpty())
            return;
        for (UserAddressDTO dto : dtos) {
            UserAddress address = new UserAddress();
            address.setUser(user);
            address.setAddressType(AddressType.valueOf(dto.getAddressType().toUpperCase()));
            address.setAddressLine1(dto.getAddressLine1());
            address.setAddressLine2(dto.getAddressLine2());
            address.setCity(dto.getCity());
            address.setPostalCode(dto.getPostalCode());
            address.setCountry(dto.getCountry());
            address.setPrimary(Boolean.TRUE.equals(dto.getIsPrimary()));
            user.getAddresses().add(address);
        }
    }

    private void buildEmergencyContacts(User user, List<UserEmergencyContactDTO> dtos) {
        if (dtos == null || dtos.isEmpty())
            return;
        AtomicInteger priority = new AtomicInteger(1);
        for (UserEmergencyContactDTO dto : dtos) {
            UserEmergencyContact contact = new UserEmergencyContact();
            contact.setUser(user);
            contact.setContactName(dto.getContactName());
            contact.setRelationship(dto.getRelationship() != null && !dto.getRelationship().isBlank()
                    ? dto.getRelationship()
                    : "OTHER");
            contact.setPhoneNumber(dto.getPhoneNumber());
            contact.setEmail(dto.getEmail());
            contact.setPrimary(Boolean.TRUE.equals(dto.getIsPrimary()));
            contact.setPriority(priority.getAndIncrement());
            user.getEmergencyContacts().add(contact);
        }
    }

    private void createOrganizationForAdmin(UUID ownerId, OrgRegisterDTO org, String email) {
        Map<String, Object> orgRequest = new HashMap<>();
        orgRequest.put("name", org.getName());
        orgRequest.put("ownerId", ownerId.toString());
        orgRequest.put("email", email);

        if (org.getLegalName() != null && !org.getLegalName().isBlank())
            orgRequest.put("legalName", org.getLegalName());
        if (org.getDescription() != null && !org.getDescription().isBlank())
            orgRequest.put("description", org.getDescription());
        if (org.getIndustry() != null && !org.getIndustry().isBlank())
            orgRequest.put("industry", org.getIndustry());
        if (org.getWebsite() != null && !org.getWebsite().isBlank())
            orgRequest.put("website", org.getWebsite());
        if (org.getLogoUrl() != null && !org.getLogoUrl().isBlank())
            orgRequest.put("logoUrl", org.getLogoUrl());
        if (org.getFoundedDate() != null && !org.getFoundedDate().isBlank())
            orgRequest.put("foundedDate", org.getFoundedDate());
        if (org.getRegistrationNumber() != null && !org.getRegistrationNumber().isBlank())
            orgRequest.put("registrationNumber", org.getRegistrationNumber());
        if (org.getTaxId() != null && !org.getTaxId().isBlank())
            orgRequest.put("taxId", org.getTaxId());
        if (org.getOrganizationSize() != null && !org.getOrganizationSize().isBlank())
            orgRequest.put("organizationSize", org.getOrganizationSize());

        try {
            orgClient.createOrganization(orgRequest).block(Duration.ofSeconds(10));
            log.info("Organization created for admin user: {}", ownerId);
        } catch (Exception e) {
            log.error("Failed to create organization for user: {}", ownerId, e);
            throw new BadRequestException("Failed to create organization: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<CheckAvailableEmailAndUsernameWhenRegisterResponse> checkAvailableEmailAndUsernameWhenRegister(
            String email, String username) {
        Boolean isEmailAvailable = userEmailService.isEmailAvailable(email);
        Boolean isUsernameAvailable = !userRepository.existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse(username);
        return APIResource.ok("Check available email and username when register successfully",
                new CheckAvailableEmailAndUsernameWhenRegisterResponse(isEmailAvailable, isUsernameAvailable));
    }

}
