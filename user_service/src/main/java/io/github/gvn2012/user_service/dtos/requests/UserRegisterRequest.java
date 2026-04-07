package io.github.gvn2012.user_service.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.github.gvn2012.user_service.dtos.OrgRegisterDTO;
import io.github.gvn2012.user_service.dtos.UserAddressDTO;
import io.github.gvn2012.user_service.dtos.UserEmergencyContactDTO;
import io.github.gvn2012.user_service.entities.enums.Gender;

@Data
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "Username must not be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Email verification id must not be null")
    private UUID emailVerificationId;

    @NotBlank(message = "Phone code must not be blank")
    private String phoneCode;

    @NotBlank(message = "Phone number must not be blank")
    @Pattern(regexp = "^(\\+?[0-9]{10,15})$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull(message = "Date of birth must not be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateBirth;

    @NotNull(message = "Profile image id must not be null")
    private UUID profileImageId;

    private List<UserAddressDTO> addresses;

    private List<UserEmergencyContactDTO> emergencyContacts;

    @NotNull(message = "Gender must not be null")
    private String gender;

    @NotNull(message = "Registration type must not be null")
    private String registrationType;

    private OrgRegisterDTO organization;

}
