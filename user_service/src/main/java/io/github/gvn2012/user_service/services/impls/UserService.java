package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.clients.AuthClient;
import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.mappers.UserDetailMapper;
import io.github.gvn2012.user_service.dtos.requests.GenerateLoginTokenRequest;
import io.github.gvn2012.user_service.dtos.responses.GenerateLoginTokenResponse;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.responses.GetUserDetailResponse;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.exception.BadRequestException;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AuthClient authClient;

    private final UserDetailMapper userDetailMapper;

    @Autowired
    public UserService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            AuthClient authClient,
            UserDetailMapper userDetailMapper
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authClient = authClient;
        this.userDetailMapper = userDetailMapper;
    }
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

}
