package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.clients.AuthClient;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.mappers.UserDetailMapper;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.CheckAvailableEmailAndUsernameWhenRegisterResponse;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.UserPhone;
import io.github.gvn2012.user_service.entities.UserProfile;
import io.github.gvn2012.user_service.entities.UserProfilePicture;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.exceptions.BadRequestException;
import io.github.gvn2012.user_service.exceptions.DataIntegrityViolationException;
import io.github.gvn2012.user_service.exceptions.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserPhoneRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserPhoneRepository userPhoneRepository;

    private final AuthClient authClient;

    private final IUserEmailService userEmailService;

    private final UserDetailMapper userDetailMapper;

    @Override
    @Transactional(readOnly = true)
    public APIResource<LoginResponse> login(LoginRequest loginRequest) {
        User user = userRepository.getByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        ensureUserCanLogin(user);

        // Test service deployment

        GenerateLoginTokenResponse tokenResponse = null;

        try {
            tokenResponse = authClient.generateToken(
                    new GenerateLoginTokenRequest(
                            user.getUsername(),
                            user.getId().toString()))
                    .block(Duration.ofSeconds(3));
        } catch (Exception e) {
            log.error("Failed to generate token", e);
            throw new BadRequestException("Failed to generate token");
        }

        if (tokenResponse == null) {
            throw new BadRequestException("Failed to generate token");
        }

        LoginResponse response = new LoginResponse(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                user.getId().toString(),
                tokenResponse.getUserRole());

        return APIResource.ok("Login successfully", response);
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<GetUserDetailResponse> getUserDetail(String userId) {

        User user = userRepository.findDetailById(UUID.fromString(userId))
                .orElseThrow(() -> new NotFoundException("User not found"));

        ensureUserAccessible(user);

        return APIResource.ok(
                "Get user detail successfully",
                userDetailMapper.toDto(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public APIResource<UserRegisterResponse> register(UserRegisterRequest request) {
        validateRegisterRequest(request);

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        @NonNull
        User user = buildUserAggregate(request, hashedPassword);

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Username, email, or phone already exists");
        }

        return APIResource.ok(
                "User created successfully",
                new UserRegisterResponse(user.getId().toString()),
                HttpStatus.CREATED);
    }

    private void validateRegisterRequest(UserRegisterRequest request) {
        if (userRepository.existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse(request.getUsername())) {
            throw new BadRequestException("The username already exists");
        }

        userEmailService.validateEmailNotUsed(request.getEmail());

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

    private User buildUserAggregate(UserRegisterRequest request, String hashedPassword) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);

        UserEmail email = new UserEmail();
        email.setUser(user);
        email.setEmail(request.getEmail().trim().toLowerCase());
        email.setVerified(true);
        email.setVerifiedAt(LocalDateTime.now());
        email.setPrimary(true);
        email.setStatus(EmailStatus.ACTIVE);

        UserPhone phone = new UserPhone();
        phone.setUser(user);
        phone.setPhoneNumber(request.getPhoneNumber());
        phone.setPrimary(true);
        phone.setVerified(true);
        phone.setVerifiedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDateOfBirth(request.getDateBirth());

        UserProfilePicture picture = new UserProfilePicture();
        picture.setUserProfile(profile);
        picture.setUrl("ghedep.xyz/default");
        picture.setPrimary(true);
        picture.setDeleted(false);

        user.getEmails().add(email);
        user.getPhones().add(phone);
        user.setProfile(profile);
        profile.getPictures().add(picture);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public APIResource<CheckAvailableEmailAndUsernameWhenRegisterResponse> checkAvailableEmailAndUsernameWhenRegister(
            String email, String username) {
        Boolean isEmailAvailable = userEmailService.isEmailAvailable(email);
        Boolean isUsernameAvailable = userRepository.existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse(username);
        return APIResource.ok("Check available email and username when register successfully",
                new CheckAvailableEmailAndUsernameWhenRegisterResponse(isEmailAvailable, isUsernameAvailable));
    }

}
