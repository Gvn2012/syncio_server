package io.github.gvn2012.user_service.services.impls;

import io.github.gvn2012.user_service.dtos.APIResource;
import io.github.gvn2012.user_service.dtos.requests.AddNewEmailRequest;
import io.github.gvn2012.user_service.dtos.requests.UpdateEmailRequest;
import io.github.gvn2012.user_service.dtos.responses.AddNewEmailResponse;
import io.github.gvn2012.user_service.dtos.responses.UpdateEmailResponse;
import io.github.gvn2012.user_service.entities.User;
import io.github.gvn2012.user_service.entities.UserEmail;
import io.github.gvn2012.user_service.entities.enums.EmailStatus;
import io.github.gvn2012.user_service.exception.BadRequestException;
import io.github.gvn2012.user_service.exception.NotFoundException;
import io.github.gvn2012.user_service.repositories.UserEmailRepository;
import io.github.gvn2012.user_service.repositories.UserRepository;
import io.github.gvn2012.user_service.services.interfaces.IUserEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserEmailService implements IUserEmailService {

    private final UserEmailRepository userEmailRepository;
    private final UserRepository userRepository;

    @Override
    public APIResource<AddNewEmailResponse> addNewEmail(UUID userId, AddNewEmailRequest request){

        Boolean emailAvailable = isEmailAvailable(request.getEmail());
        if(!emailAvailable) {
            throw new BadRequestException("Email already existed");
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found")
        );

        UserEmail userEmail = new UserEmail();
        userEmail.setUser(user);
        userEmail.setEmail(request.getEmail());
        userEmail.setPrimary(false);
        userEmail.setVerified(true);
        userEmail.setVerifiedAt(LocalDateTime.now());
        userEmail.setStatus(EmailStatus.ACTIVE);

        userEmailRepository.save(userEmail);

        return APIResource.ok(
                "Added email successfully",
                new AddNewEmailResponse(userEmail.getId().toString()),
                HttpStatus.CREATED
        );
    }

    public Boolean isEmailAvailable(String email) {
        return !userEmailRepository.existsByEmailAndStatusNot(email, EmailStatus.REMOVED);
    }

    public APIResource<UpdateEmailResponse> updateEmail(UUID userId, UUID emailId, UpdateEmailRequest request) {


        UpdateEmailResponse response = new UpdateEmailResponse();
            return APIResource.ok(
                "Update email successfully",
                    response,
                    HttpStatus.OK

        );
    }
}
