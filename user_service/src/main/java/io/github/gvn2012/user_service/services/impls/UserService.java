package io.github.gvn2012.user_service.services.impls;


import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.LoginRequest;
import io.github.gvn2012.user_service.dtos.responses.LoginResponse;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.UserServiceInterface;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService implements UserServiceInterface {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public APIResource<LoginResponse> login(LoginRequest loginRequest) {
        try {
            if (loginRequest == null) {
                throw new BadRequestException("Invalid request");
            }

            if (!loginRequest.getUsername().equals("admin") || !loginRequest.getPassword().equals("admin")) {
                throw new BadRequestException("Invalid credentials");
            }

            String password = "admin";
            log.info("Login password: {}", passwordEncoder.encode(password));
            return APIResource.ok("Login successfully", new LoginResponse("a", "b", "c"));
        } catch (BadRequestException e) {
            return APIResource.error("BAD_REQUEST", e.getMessage(), HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


}
