package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.clients.AuthClient;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.mappers.UserDetailMapper;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.requests.UserRegisterRequest;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.dtos.responses.UserRegisterResponse;
import io.github.gvn2012.user_service.entities.*;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.exception.BadRequestException;
import io.github.gvn2012.user_service.exception.DataIntegrityViolationException;
import io.github.gvn2012.user_service.repositories.*;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final UserEmailRepository userEmailRepository;
    private final UserPhoneRepository userPhoneRepository;
    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfileRepository userProfileRepository;

    private final AuthClient authClient;

    private final UserDetailMapper userDetailMapper;

    @Autowired
    public UserService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserEmailRepository userEmailRepository,
            UserPhoneRepository userPhoneRepository,
            UserProfilePictureRepository userProfilePictureRepository,
            UserProfileRepository userProfileRepository,
            AuthClient authClient,
            UserDetailMapper userDetailMapper

    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userEmailRepository = userEmailRepository;
        this.userPhoneRepository = userPhoneRepository;
        this.userProfilePictureRepository = userProfilePictureRepository;
        this.userProfileRepository = userProfileRepository;
        this.authClient = authClient;
        this.userDetailMapper = userDetailMapper;
    }

    @Override
    public APIResource<LoginResponse> login(LoginRequest loginRequest) {

        validateRequest(loginRequest);

        User user = userRepository.getByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        validatePassword(loginRequest, user);

        if (!isUserActive(user)) {
            throw new BadRequestException("Account is not active");
        }

        if (isUserSoftDeleted(user)) {
            throw new BadRequestException("Account is soft deleted");
        }

        if (isUserHardDeleted(user)) {
            throw new BadRequestException("Account is hard deleted");
        }

        if (isUserSuspended(user)) {
            throw new BadRequestException("Account is suspended");
        }

        if (isUserBanned(user)) {
            throw new BadRequestException("Account is banned");
        }

        GenerateLoginTokenRequest request = new GenerateLoginTokenRequest(
                loginRequest.getUsername(),
                user.getId().toString()
        );

        GenerateLoginTokenResponse loginToken =
                authClient.generateToken(request)
                        .block(Duration.ofSeconds(3));

        if (loginToken == null) {
            throw new BadRequestException("Failed to generate token");
        }

        LoginResponse loginResponse = new LoginResponse(
                loginToken.getAccessToken(),
                loginToken.getRefreshToken(),
                user.getId().toString()

        );

        return APIResource.ok("Login successfully", loginResponse);
    }

    public void validateRequest(LoginRequest loginRequest) {
        if (loginRequest == null) {
            throw new BadRequestException("Missing username or password");
        }
    }

    public void validatePassword(@NonNull LoginRequest loginRequest,@NonNull User user) {
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid password");
        }
    }

    public Boolean isUserSoftDeleted(@NonNull User user) {
        return user.getSoftDeleted();
    }

    public Boolean isUserHardDeleted(@NonNull User user) {
        return user.getHardDeleted();
    }

    public Boolean isUserActive(@NonNull User user) {
        return user.getActive();
    }

    public Boolean isUserSuspended(@NonNull User user) {
        return user.getSuspended();
    }

    public Boolean isUserBanned(@NonNull User user) {
        return user.getBanned();
    }

    public APIResource<GetUserDetailResponse> getUserDetail(String userId) {

        User user = userRepository.findDetailById(UUID.fromString(userId))
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (isUserSoftDeleted(user)) {
            throw new BadRequestException("Account is soft deleted");
        }

        if (isUserHardDeleted(user)) {
            throw new BadRequestException("Account is hard deleted");
        }

        return APIResource.ok(
                "Get user detail successfully",
                userDetailMapper.toDto(user)
        );
    }

    @Override
    @Transactional(
            rollbackFor = {
                    RuntimeException.class,
                    BadRequestException.class,
                    DataIntegrityViolationException.class}
    )
    public APIResource<UserRegisterResponse> register(UserRegisterRequest request) {

        if (userRepository.existsByUsernameAndSoftDeletedFalseAndHardDeletedFalse(request.getUsername())) {
            throw new BadRequestException("The username already exists");
        }

        if (userEmailRepository.existsByEmailAndStatusNot(request.getEmail(), EmailStatus.REMOVED)) {
            throw new BadRequestException("Email already exists");
        }

        if (userPhoneRepository.existsUserPhoneByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone already exists");
        }

        validatePassword(request.getPassword());

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);

        userRepository.save(user);

        UserEmail email = new UserEmail();
        email.setUser(user);
        email.setEmail(request.getEmail());
        email.setVerified(true);
        email.setVerifiedAt(LocalDateTime.now());
        email.setPrimary(true);
        email.setStatus(EmailStatus.ACTIVE);

        userEmailRepository.save(email);

        UserPhone phone = new UserPhone();
        phone.setUser(user);
        phone.setPhoneNumber(request.getPhoneNumber());
        phone.setPrimary(true);
        phone.setVerified(true);

        userPhoneRepository.save(phone);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDateOfBirth(request.getDateBirth());

        userProfileRepository.save(profile);

        UserProfilePicture picture = new UserProfilePicture();
        picture.setUserProfile(profile);
        picture.setUrl("ghedep.xyz/default");
        picture.setPrimary(true);
        picture.setDeleted(false);

        userProfilePictureRepository.save(picture);

        return APIResource.ok(
                "User created successfully",
                new UserRegisterResponse(user.getId().toString()),
                HttpStatus.CREATED
        );
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required");
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
        if (!password.matches(passwordRegex)) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character"
            );
        }
    }



}
